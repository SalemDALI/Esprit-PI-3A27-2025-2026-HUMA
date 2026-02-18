package com.huma.model;

public class Candidat {
    private int id;
    private String cv;

    public Candidat() {
    }

    public Candidat(int id, String cv) {
        this.id = id;
        this.cv = cv;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }
}
