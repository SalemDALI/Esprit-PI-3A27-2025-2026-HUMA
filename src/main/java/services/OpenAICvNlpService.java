package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.CvAnalysisResult;
import models.OffreEmploi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAICvNlpService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+");

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public CvAnalysisResult analyzeCvText(String cvText, OffreEmploi offre) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return heuristicFallback(cvText);
        }

        String model = System.getenv("OPENAI_MODEL");
        if (model == null || model.isBlank()) {
            model = "gpt-4o-mini";
        }

        try {
            String payload = buildPayload(model, cvText, offre);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(45))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return heuristicFallback(cvText);
            }

            JsonNode root = mapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                return heuristicFallback(cvText);
            }

            return parseModelJson(content, cvText);
        } catch (Exception e) {
            return heuristicFallback(cvText);
        }
    }

    private String buildPayload(String model, String cvText, OffreEmploi offre) throws JsonProcessingException {
        String prompt = "Analyse ce CV et retourne strictement un JSON valide avec les champs suivants: " +
                "full_name, email, phone, location, years_experience, skills(array), languages(array), " +
                "education(array), certifications(array), summary. " +
                "Ne retourne aucun texte hors JSON. " +
                "Contexte offre: titre='" + safe(offre == null ? null : offre.getTitre()) +
                "', description='" + safe(offre == null ? null : offre.getDescription()) + "'.\n\n" +
                "CV:\n" + trimForPrompt(cvText);

        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", model);
        payload.put("temperature", 0);
        payload.set("response_format", mapper.createObjectNode().put("type", "json_object"));
        payload.set("messages", mapper.createArrayNode()
                .add(mapper.createObjectNode()
                        .put("role", "system")
                        .put("content", "Tu extrais des donnees structurees de CV en JSON."))
                .add(mapper.createObjectNode()
                        .put("role", "user")
                        .put("content", prompt)));
        return payload.toString();
    }

    private CvAnalysisResult parseModelJson(String content, String cvText) throws JsonProcessingException {
        JsonNode node = mapper.readTree(content);
        CvAnalysisResult result = new CvAnalysisResult();
        result.setFullName(asText(node, "full_name"));
        result.setEmail(asText(node, "email"));
        result.setPhone(asText(node, "phone"));
        result.setLocation(asText(node, "location"));
        result.setYearsExperience(asDouble(node, "years_experience"));
        result.setSkills(asList(node, "skills"));
        result.setLanguages(asList(node, "languages"));
        result.setEducation(asList(node, "education"));
        result.setCertifications(asList(node, "certifications"));
        result.setSummary(asText(node, "summary"));

        if ((result.getEmail() == null || result.getEmail().isBlank()) && cvText != null) {
            result.setEmail(extractEmail(cvText));
        }

        return result;
    }

    private CvAnalysisResult heuristicFallback(String cvText) {
        CvAnalysisResult result = new CvAnalysisResult();
        result.setEmail(extractEmail(cvText));
        result.setYearsExperience(extractYearsExperience(cvText));
        result.setSkills(extractSkills(cvText));
        result.setLanguages(extractLanguages(cvText));
        result.setEducation(extractEducation(cvText));
        result.setSummary("Extraction locale (fallback) sans appel API externe.");
        return result;
    }

    private String extractEmail(String text) {
        if (text == null) {
            return "";
        }
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : "";
    }

    private double extractYearsExperience(String text) {
        if (text == null) {
            return 0;
        }
        Matcher matcher = Pattern.compile("(\\d{1,2})\\s*(ans|years)", Pattern.CASE_INSENSITIVE).matcher(text);
        double max = 0;
        while (matcher.find()) {
            max = Math.max(max, Double.parseDouble(matcher.group(1)));
        }
        return max;
    }

    private List<String> extractSkills(String text) {
        String[] dictionary = {
                "java", "spring", "sql", "mysql", "postgresql", "python", "javascript", "react", "node", "docker",
                "kubernetes", "aws", "azure", "git", "linux", "html", "css", "rest", "api", "hibernate"
        };
        return keywordMatch(text, dictionary);
    }

    private List<String> extractLanguages(String text) {
        String[] dictionary = {"francais", "anglais", "arabe", "espagnol", "allemand", "italien"};
        return keywordMatch(text, dictionary);
    }

    private List<String> extractEducation(String text) {
        String[] dictionary = {"licence", "master", "ingenieur", "doctorat", "bachelor", "mba"};
        return keywordMatch(text, dictionary);
    }

    private List<String> keywordMatch(String text, String[] dictionary) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return out;
        }

        String lower = text.toLowerCase(Locale.ROOT);
        for (String keyword : dictionary) {
            if (lower.contains(keyword)) {
                out.add(keyword);
            }
        }
        return out;
    }

    private String trimForPrompt(String text) {
        if (text == null) {
            return "";
        }
        int max = 12000;
        return text.length() <= max ? text : text.substring(0, max);
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\"", "'");
    }

    private String asText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
    }

    private double asDouble(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? 0 : value.asDouble(0);
    }

    private List<String> asList(JsonNode node, String field) {
        List<String> values = new ArrayList<>();
        JsonNode array = node.path(field);
        if (!array.isArray()) {
            return values;
        }
        for (JsonNode item : array) {
            String value = item.asText("").trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }
}

