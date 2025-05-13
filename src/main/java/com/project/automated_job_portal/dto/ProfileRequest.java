package com.project.automated_job_portal.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class ProfileRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String zipCode;
    private String experienceLevel;
    private String areaOfInterest;
    private MultipartFile resume;
    private List<String> skills = new ArrayList<>();
    private List<String> preferredRoles = new ArrayList<>();
    private List<String> preferredLocations = new ArrayList<>();
    private List<String> communitiesOfInterest = new ArrayList<>();
    private String referralSource;
    private String gender;
    private Boolean hasDisability;
    private List<EducationRequest> educationHistory = new ArrayList<>();
    private List<WorkExperienceRequest> workExperience = new ArrayList<>();

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
    public String getAreaOfInterest() { return areaOfInterest; }
    public void setAreaOfInterest(String areaOfInterest) { this.areaOfInterest = areaOfInterest; }
    public MultipartFile getResume() { return resume; }
    public void setResume(MultipartFile resume) { this.resume = resume; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public List<String> getPreferredRoles() { return preferredRoles; }
    public void setPreferredRoles(List<String> preferredRoles) { this.preferredRoles = preferredRoles; }
    public List<String> getPreferredLocations() { return preferredLocations; }
    public void setPreferredLocations(List<String> preferredLocations) { this.preferredLocations = preferredLocations; }
    public List<String> getCommunitiesOfInterest() { return communitiesOfInterest; }
    public void setCommunitiesOfInterest(List<String> communitiesOfInterest) { this.communitiesOfInterest = communitiesOfInterest; }
    public String getReferralSource() { return referralSource; }
    public void setReferralSource(String referralSource) { this.referralSource = referralSource; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Boolean getHasDisability() { return hasDisability; }
    public void setHasDisability(Boolean hasDisability) { this.hasDisability = hasDisability; }
    public List<EducationRequest> getEducationHistory() { return educationHistory; }
    public void setEducationHistory(List<EducationRequest> educationHistory) { this.educationHistory = educationHistory; }
    public List<WorkExperienceRequest> getWorkExperience() { return workExperience; }
    public void setWorkExperience(List<WorkExperienceRequest> workExperience) { this.workExperience = workExperience; }
}