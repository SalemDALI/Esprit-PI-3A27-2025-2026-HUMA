package model;

import java.sql.Date;

public class Formation {
    private int id;
    private String sujet;
    private String formateur;
    private String type;
    private Date dateDebut; // Correspond à date_debut en SQL
    private int duree;
    private String localisation;

    // Constructeur vide (nécessaire pour certains frameworks et tests)
    public Formation() {
    }

    // Constructeur complet (Utilisé pour l'affichage depuis la DB)
    public Formation(int id, String sujet, String formateur, String type, Date dateDebut, int duree, String localisation) {
        this.id = id;
        this.sujet = sujet;
        this.formateur = formateur;
        this.type = type;
        this.dateDebut = dateDebut;
        this.duree = duree;
        this.localisation = localisation;
    }

    // Constructeur sans ID (Utilisé pour l'ajout/insertion)
    public Formation(String sujet, String formateur, String type, Date dateDebut, int duree, String localisation) {
        this.sujet = sujet;
        this.formateur = formateur;
        this.type = type;
        this.dateDebut = dateDebut;
        this.duree = duree;
        this.localisation = localisation;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public String getFormateur() {
        return formateur;
    }

    public void setFormateur(String formateur) {
        this.formateur = formateur;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }


}