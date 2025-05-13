package com.project.automated_job_portal.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "work_experience")
@Data
public class WorkExperienceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobTitle;
    private String company;
    private String startDate;
    private String endDate;
    private String responsibilities;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}