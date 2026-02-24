package models;

import java.time.LocalDate;

public class Candidature {

    private int id;
    private LocalDate dateCandidature;
    private String statut;
    private int candidatId;
    private int offreId;
    private String offreTitre;
    private String cheminCv;
    private String candidatNom;
    private String candidatEmail;

    public Candidature() {}

    public Candidature(int candidatId, int offreId, LocalDate dateCandidature, String statut) {
        this.candidatId = candidatId;
        this.offreId = offreId;
        this.dateCandidature = dateCandidature;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDateCandidature() { return dateCandidature; }
    public void setDateCandidature(LocalDate dateCandidature) { this.dateCandidature = dateCandidature; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getCandidatId() { return candidatId; }
    public void setCandidatId(int candidatId) { this.candidatId = candidatId; }

    public int getOffreId() { return offreId; }
    public void setOffreId(int offreId) { this.offreId = offreId; }

    public String getOffreTitre() { return offreTitre; }
    public void setOffreTitre(String offreTitre) { this.offreTitre = offreTitre; }

    public String getCheminCv() { return cheminCv; }
    public void setCheminCv(String cheminCv) { this.cheminCv = cheminCv; }

    public String getCandidatNom() { return candidatNom; }
    public void setCandidatNom(String candidatNom) { this.candidatNom = candidatNom; }

    public String getCandidatEmail() { return candidatEmail; }
    public void setCandidatEmail(String candidatEmail) { this.candidatEmail = candidatEmail; }
}
