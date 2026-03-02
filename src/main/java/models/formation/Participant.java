package models.formation;

import java.sql.Date;

public class Participant {
    private int id;
    private Date dateInscription;
    private String resultat;
    private int employeId;
    private int formationId;
    private String nomFormation;
    private String nomEmploye;

    public Participant() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Date getDateInscription() { return dateInscription; }
    public void setDateInscription(Date dateInscription) { this.dateInscription = dateInscription; }
    public String getResultat() { return resultat; }
    public void setResultat(String resultat) { this.resultat = resultat; }
    public int getEmployeId() { return employeId; }
    public void setEmployeId(int employeId) { this.employeId = employeId; }
    public int getFormationId() { return formationId; }
    public void setFormationId(int formationId) { this.formationId = formationId; }
    public String getNomFormation() { return nomFormation; }
    public void setNomFormation(String nomFormation) { this.nomFormation = nomFormation; }
    public String getNomEmploye() { return nomEmploye; }
    public void setNomEmploye(String nomEmploye) { this.nomEmploye = nomEmploye; }
}
