package models.formation;

import java.sql.Date;

public class Formation {
    private int id;
    private String sujet;
    private String formateur;
    private String type;
    private Date dateDebut;
    private int duree;
    private String localisation;
    private int adminId;

    public Formation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }
    public String getFormateur() { return formateur; }
    public void setFormateur(String formateur) { this.formateur = formateur; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }
    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }
    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }
    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
}
