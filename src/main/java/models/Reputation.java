package models;

import java.time.LocalDateTime;

public class Reputation {
    private int id;
    private int userId;
    private int totalScore;
    private String badge;
    private LocalDateTime lastUpdated;
    private String nom;
    private String prenom;

    public Reputation() {}

    public Reputation(int userId, int totalScore, String badge) {
        this.userId = userId;
        this.totalScore = totalScore;
        this.badge = badge;
        this.lastUpdated = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
}