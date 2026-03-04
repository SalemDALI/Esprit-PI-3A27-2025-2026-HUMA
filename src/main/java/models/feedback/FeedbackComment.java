package models.feedback;

import java.sql.Timestamp;

public class FeedbackComment {

    private int id;
    private int feedbackId;
    private int userId;
    private String contenu;
    private Timestamp dateCreation;
    private String userNom; // optional, for display (joined or set by service)

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public String getUserNom() { return userNom; }
    public void setUserNom(String userNom) { this.userNom = userNom; }
}
