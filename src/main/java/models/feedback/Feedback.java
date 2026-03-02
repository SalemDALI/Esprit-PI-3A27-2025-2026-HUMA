package models.feedback;


public class Feedback {

    private int id;
    private String message;
    private String email;
    private String statut;

    public Feedback() {}

    public Feedback(int id, String message, String email, String statut) {
        this.id = id;
        this.message = message;
        this.email = email;
        this.statut = statut;
    }

    public Feedback(String message, String email, String statut) {
        this.message = message;
        this.email = email;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}