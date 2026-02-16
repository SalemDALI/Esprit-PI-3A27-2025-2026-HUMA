package com.huma.model;

import java.time.LocalDate;

public class Candidature {
    private int id;
    private LocalDate dateCandidature;
    private String statut;
    private int candidatId;
    private int offreId;
    private String candidatNom;
    private String offreTitre;

    public Candidature() {
    }

    public Candidature(int id, LocalDate dateCandidature, String statut, int candidatId, int offreId) {
        this.id = id;
        this.dateCandidature = dateCandidature;
        this.statut = statut;
        this.candidatId = candidatId;
        this.offreId = offreId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDateCandidature() {
        return dateCandidature;
    }

    public void setDateCandidature(LocalDate dateCandidature) {
        this.dateCandidature = dateCandidature;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getCandidatId() {
        return candidatId;
    }

    public void setCandidatId(int candidatId) {
        this.candidatId = candidatId;
    }

    public int getOffreId() {
        return offreId;
    }

    public void setOffreId(int offreId) {
        this.offreId = offreId;
    }

    public String getCandidatNom() {
        return candidatNom;
    }

    public void setCandidatNom(String candidatNom) {
        this.candidatNom = candidatNom;
    }

    public String getOffreTitre() {
        return offreTitre;
    }

    public void setOffreTitre(String offreTitre) {
        this.offreTitre = offreTitre;
    }
}
