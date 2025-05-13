package com.project.automated_job_portal.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "education")
@Data
public class EducationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String degree;
    private String institution;
    private String startDate;
    private String endDate;
    private String completionYear; // Added to match DataInitializer

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}