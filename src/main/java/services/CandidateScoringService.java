package services;

import models.CandidateScoringResult;
import models.Candidature;
import models.CvAnalysisResult;
import models.OffreEmploi;
import utils.CvRankingStorageUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class CandidateScoringService {

    private final ServiceCandidature serviceCandidature;
    private final ServiceOffre serviceOffre;
    private final CvPdfTextExtractor pdfTextExtractor;
    private final OpenAICvNlpService nlpService;

    public CandidateScoringService() {
        this(new ServiceCandidature(), new ServiceOffre(), new CvPdfTextExtractor(), new OpenAICvNlpService());
    }

    public CandidateScoringService(ServiceCandidature serviceCandidature,
                                   ServiceOffre serviceOffre,
                                   CvPdfTextExtractor pdfTextExtractor,
                                   OpenAICvNlpService nlpService) {
        this.serviceCandidature = serviceCandidature;
        this.serviceOffre = serviceOffre;
        this.pdfTextExtractor = pdfTextExtractor;
        this.nlpService = nlpService;
    }

    public List<CandidateScoringResult> rankTopCandidatesForOffer(int offreId, int topN) {
        OffreEmploi offre = serviceOffre.getById(offreId);
        if (offre == null) {
            return List.of();
        }

        List<CandidateScoringResult> scored = new ArrayList<>();
        List<Candidature> candidatures = serviceCandidature.getByOffreId(offreId);

        for (Candidature candidature : candidatures) {
            CandidateScoringResult result = scoreCandidature(candidature, offre);
            if (result != null) {
                scored.add(result);
            }
        }

        List<CandidateScoringResult> sorted = scored.stream()
                .sorted(Comparator.comparingInt(CandidateScoringResult::getScoreGlobal).reversed())
                .collect(Collectors.toList());

        if (topN <= 0 || topN >= sorted.size()) {
            return sorted;
        }
        return sorted.subList(0, topN);
    }

    public String rankTopCandidatesForOfferAndSaveJson(int offreId, int topN) throws java.io.IOException {
        OffreEmploi offre = serviceOffre.getById(offreId);
        if (offre == null) {
            throw new IllegalArgumentException("Offre introuvable: " + offreId);
        }

        List<CandidateScoringResult> ranking = rankTopCandidatesForOffer(offreId, topN);
        return CvRankingStorageUtil.saveRankingAsJson(offre, ranking);
    }

    private CandidateScoringResult scoreCandidature(Candidature candidature, OffreEmploi offre) {
        if (candidature.getCheminCv() == null || candidature.getCheminCv().isBlank()) {
            return null;
        }

        try {
            String cvText = pdfTextExtractor.extractText(candidature.getCheminCv());
            CvAnalysisResult analysis = nlpService.analyzeCvText(cvText, offre);

            int scoreSkills = computeSkillsScore(analysis, cvText, offre);
            int scoreExperience = computeExperienceScore(analysis, offre);
            int scoreEducation = computeEducationScore(analysis);
            int scoreGlobal = Math.min(100, scoreSkills + scoreExperience + scoreEducation);

            CandidateScoringResult result = new CandidateScoringResult();
            result.setCandidatureId(candidature.getId());
            result.setCandidatId(candidature.getCandidatId());
            result.setCandidatNom(candidature.getCandidatNom());
            result.setCandidatEmail(candidature.getCandidatEmail());
            result.setOffreId(candidature.getOffreId());
            result.setScoreSkills(scoreSkills);
            result.setScoreExperience(scoreExperience);
            result.setScoreEducation(scoreEducation);
            result.setScoreGlobal(scoreGlobal);
            result.setCvAnalysis(analysis);
            result.setCommentaire(buildComment(scoreSkills, scoreExperience, scoreEducation));
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private int computeSkillsScore(CvAnalysisResult analysis, String cvText, OffreEmploi offre) {
        Set<String> offerKeywords = extractOfferKeywords(offre);
        if (offerKeywords.isEmpty()) {
            return 30;
        }

        Set<String> candidateTokens = new HashSet<>();
        for (String skill : analysis.getSkills()) {
            candidateTokens.add(normalize(skill));
        }

        String cvLower = cvText == null ? "" : cvText.toLowerCase(Locale.ROOT);
        int matched = 0;
        for (String keyword : offerKeywords) {
            if (candidateTokens.contains(keyword) || cvLower.contains(keyword)) {
                matched++;
            }
        }

        double ratio = (double) matched / offerKeywords.size();
        return (int) Math.round(ratio * 60);
    }

    private int computeExperienceScore(CvAnalysisResult analysis, OffreEmploi offre) {
        double targetYears = inferTargetYears(offre);
        if (targetYears <= 0) {
            targetYears = 3;
        }

        double ratio = analysis.getYearsExperience() / targetYears;
        ratio = Math.max(0, Math.min(1.2, ratio));
        return (int) Math.round(Math.min(1, ratio) * 25);
    }

    private int computeEducationScore(CvAnalysisResult analysis) {
        String edu = String.join(" ", analysis.getEducation()).toLowerCase(Locale.ROOT);
        if (edu.contains("doctor") || edu.contains("phd")) {
            return 15;
        }
        if (edu.contains("master") || edu.contains("ingenieur")) {
            return 12;
        }
        if (edu.contains("licence") || edu.contains("bachelor")) {
            return 8;
        }
        return 4;
    }

    private double inferTargetYears(OffreEmploi offre) {
        String text = ((offre.getTitre() == null ? "" : offre.getTitre()) + " " +
                (offre.getDescription() == null ? "" : offre.getDescription())).toLowerCase(Locale.ROOT);

        if (text.contains("senior") || text.contains("lead")) {
            return 5;
        }
        if (text.contains("junior") || text.contains("intern") || text.contains("stage")) {
            return 1;
        }
        return 3;
    }

    private Set<String> extractOfferKeywords(OffreEmploi offre) {
        String[] dictionary = {
                "java", "spring", "sql", "mysql", "postgresql", "python", "javascript", "react", "node", "docker",
                "kubernetes", "aws", "azure", "git", "linux", "api", "rest", "scrum", "agile", "devops"
        };

        String text = ((offre.getTitre() == null ? "" : offre.getTitre()) + " " +
                (offre.getDescription() == null ? "" : offre.getDescription())).toLowerCase(Locale.ROOT);

        Set<String> out = new HashSet<>();
        for (String keyword : dictionary) {
            if (text.contains(keyword)) {
                out.add(keyword);
            }
        }

        if (out.isEmpty()) {
            out.add("java");
            out.add("sql");
            out.add("api");
        }

        return out;
    }

    private String buildComment(int skills, int experience, int education) {
        return "skills=" + skills + "/60, experience=" + experience + "/25, education=" + education + "/15";
    }

    private String normalize(String input) {
        return input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
    }
}
