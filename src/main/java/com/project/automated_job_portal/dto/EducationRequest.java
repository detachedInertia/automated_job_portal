package com.project.automated_job_portal.dto;

public class EducationRequest {
    private String degree;
    private String institution;
    private String startDate;
    private String endDate;

    // Getters and Setters
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}