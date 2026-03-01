package models;

public class CandidateScoringResult {


        private int candidatureId;
        private int candidatId;
        private String candidatNom;
        private String candidatEmail;
        private int offreId;
        private int scoreGlobal;
        private int scoreSkills;
        private int scoreExperience;
        private int scoreEducation;
        private CvAnalysisResult cvAnalysis;
        private String commentaire;

        public int getCandidatureId() {
            return candidatureId;
        }

        public void setCandidatureId(int candidatureId) {
            this.candidatureId = candidatureId;
        }

        public int getCandidatId() {
            return candidatId;
        }

        public void setCandidatId(int candidatId) {
            this.candidatId = candidatId;
        }

        public String getCandidatNom() {
            return candidatNom;
        }

        public void setCandidatNom(String candidatNom) {
            this.candidatNom = candidatNom;
        }

        public String getCandidatEmail() {
            return candidatEmail;
        }

        public void setCandidatEmail(String candidatEmail) {
            this.candidatEmail = candidatEmail;
        }

        public int getOffreId() {
            return offreId;
        }

        public void setOffreId(int offreId) {
            this.offreId = offreId;
        }

        public int getScoreGlobal() {
            return scoreGlobal;
        }

        public void setScoreGlobal(int scoreGlobal) {
            this.scoreGlobal = scoreGlobal;
        }

        public int getScoreSkills() {
            return scoreSkills;
        }

        public void setScoreSkills(int scoreSkills) {
            this.scoreSkills = scoreSkills;
        }

        public int getScoreExperience() {
            return scoreExperience;
        }

        public void setScoreExperience(int scoreExperience) {
            this.scoreExperience = scoreExperience;
        }

        public int getScoreEducation() {
            return scoreEducation;
        }

        public void setScoreEducation(int scoreEducation) {
            this.scoreEducation = scoreEducation;
        }

        public CvAnalysisResult getCvAnalysis() {
            return cvAnalysis;
        }

        public void setCvAnalysis(CvAnalysisResult cvAnalysis) {
            this.cvAnalysis = cvAnalysis;
        }

        public String getCommentaire() {
            return commentaire;
        }

        public void setCommentaire(String commentaire) {
            this.commentaire = commentaire;
        }
    }


