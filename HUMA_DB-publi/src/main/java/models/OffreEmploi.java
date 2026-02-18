package models;

import java.time.LocalDate;

public class OffreEmploi {

    private int id;
    private String titre;
    private String description;
    private String departement;
    private LocalDate datePublication;
    private String typeContrat;
    private int nombrePostes;
    private int adminId;

    public OffreEmploi() {
    }

    public OffreEmploi(int id, String titre, String description, String departement,
                       LocalDate datePublication, String typeContrat,
                       int nombrePostes, int adminId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.departement = departement;
        this.datePublication = datePublication;
        this.typeContrat = typeContrat;
        this.nombrePostes = nombrePostes;
        this.adminId = adminId;
    }

    // GETTERS & SETTERS

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public LocalDate getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDate datePublication) {
        this.datePublication = datePublication;
    }

    public String getTypeContrat() {
        return typeContrat;
    }

    public void setTypeContrat(String typeContrat) {
        this.typeContrat = typeContrat;
    }

    public int getNombrePostes() {
        return nombrePostes;
    }

    public void setNombrePostes(int nombrePostes) {
        this.nombrePostes = nombrePostes;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    @Override
    public String toString() {
        return titre;
    }
}
