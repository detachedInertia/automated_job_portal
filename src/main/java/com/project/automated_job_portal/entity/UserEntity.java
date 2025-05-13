package com.project.automated_job_portal.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // String for now, can be enum later

    private String firstName;
    private String lastName;
    private String address;
    private String zipCode;
    private String experienceLevel;
    private String areaOfInterest;

    private String resumePath;

    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>(); // Simple list of skill names

    private String referralSource;
    private String gender;
    private Boolean hasDisability;

    // Added fields to match DataInitializer
    private LocalDate dateOfBirth;
    private String experience;
    private String currentCompany;
    private String location;
    private String resumeHeadline;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EducationEntity> educationHistory = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkExperienceEntity> workExperience = new ArrayList<>();

    @OneToMany(mappedBy = "postedBy")
    private List<JobEntity> postedJobs;

    @OneToMany(mappedBy = "user")
    private List<ApplicationEntity> applications;

    @ElementCollection
    @CollectionTable(name = "user_preferences", joinColumns = @JoinColumn(name = "user_id"))
    private List<String> preferredRoles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_locations", joinColumns = @JoinColumn(name = "user_id"))
    private List<String> preferredLocations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_communities", joinColumns = @JoinColumn(name = "user_id"))
    private List<String> communitiesOfInterest = new ArrayList<>();

    @Override
    public String toString() {
        return "UserEntity{id=" + id + ", email=" + email + ", firstName=" + firstName + ", lastName=" + lastName + "}";
    }
}