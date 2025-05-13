package com.project.automated_job_portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
public class JobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 100, message = "Job title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String company;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 100, message = "Location must be between 2 and 100 characters")
    private String location;

    @NotBlank(message = "Experience level is required")
    @Size(min = 2, max = 50, message = "Experience level must be between 2 and 50 characters")
    private String experienceLevel;

    @NotNull(message = "Salary is required")
    private Double salary;

    @ManyToOne
    @JoinColumn(name = "posted_by", nullable = false)
    private UserEntity postedBy;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationEntity> applications;

    private LocalDateTime createdAt;

    @NotBlank(message = "Job type is required")
    @Size(min = 2, max = 20, message = "Job type must be between 2 and 20 characters")
    @Column(name = "type", length = 20)
    private String type = "JOB";

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}