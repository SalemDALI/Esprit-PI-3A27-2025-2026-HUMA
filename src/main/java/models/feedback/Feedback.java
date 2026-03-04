package models.feedback;


public class Feedback {

    private int id;
    private String contenu;
    private java.sql.Timestamp dateEnvoi;
    private boolean estAnonyme;
    private Integer employeId;
    private Integer adminId;
    private String category;
    private String status;

    public Feedback() {}

    public Feedback(int id, String contenu, java.sql.Timestamp dateEnvoi, boolean estAnonyme, Integer employeId, Integer adminId) {
        this.id = id;
        this.contenu = contenu;
        this.dateEnvoi = dateEnvoi;
        this.estAnonyme = estAnonyme;
        this.employeId = employeId;
        this.adminId = adminId;
    }

    public Feedback(String contenu, boolean estAnonyme, Integer employeId) {
        this.contenu = contenu;
        this.estAnonyme = estAnonyme;
        this.employeId = employeId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public java.sql.Timestamp getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(java.sql.Timestamp dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public boolean isEstAnonyme() { return estAnonyme; }
    public void setEstAnonyme(boolean estAnonyme) { this.estAnonyme = estAnonyme; }

    public Integer getEmployeId() { return employeId; }
    public void setEmployeId(Integer employeId) { this.employeId = employeId; }

    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}