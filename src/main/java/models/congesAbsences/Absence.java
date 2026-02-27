package models.congesAbsences;

import java.time.LocalDate;

public class Absence {

    private int id;
    private int employeId;
    private String employeNom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String typeAbsence; // CONGE, MALADIE, RETARD
    private String statut;      // EN_ATTENTE, ACCEPTE, REFUSE
    private LocalDate dateDemandeConge;
    private Integer managerValidationId;
    private String commentaireValidation;
    private LocalDate dateValidation;

    public Absence() {}

    public Absence(int employeId, LocalDate dateDebut, LocalDate dateFin,
                   String typeAbsence, String statut) {
        this.employeId = employeId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.typeAbsence = typeAbsence;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeId() { return employeId; }
    public void setEmployeId(int employeId) { this.employeId = employeId; }

    public String getEmployeNom() { return employeNom; }
    public void setEmployeNom(String employeNom) { this.employeNom = employeNom; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getTypeAbsence() { return typeAbsence; }
    public void setTypeAbsence(String typeAbsence) { this.typeAbsence = typeAbsence; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDate getDateDemandeConge() { return dateDemandeConge; }
    public void setDateDemandeConge(LocalDate dateDemandeConge) { this.dateDemandeConge = dateDemandeConge; }

    public Integer getManagerValidationId() { return managerValidationId; }
    public void setManagerValidationId(Integer managerValidationId) { this.managerValidationId = managerValidationId; }

    public String getCommentaireValidation() { return commentaireValidation; }
    public void setCommentaireValidation(String commentaireValidation) { this.commentaireValidation = commentaireValidation; }

    public LocalDate getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDate dateValidation) { this.dateValidation = dateValidation; }
}
