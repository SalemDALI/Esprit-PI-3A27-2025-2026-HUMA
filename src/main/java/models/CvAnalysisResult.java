package models;

import java.util.ArrayList;
import java.util.List;

public class CvAnalysisResult {

    private String fullName;
    private String email;
    private String phone;
    private String location;
    private double yearsExperience;
    private List<String> skills = new ArrayList<>();
    private List<String> languages = new ArrayList<>();
    private List<String> education = new ArrayList<>();
    private List<String> certifications = new ArrayList<>();
    private String summary;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(double yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills == null ? new ArrayList<>() : skills;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages == null ? new ArrayList<>() : languages;
    }

    public List<String> getEducation() {
        return education;
    }

    public void setEducation(List<String> education) {
        this.education = education == null ? new ArrayList<>() : education;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications == null ? new ArrayList<>() : certifications;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
