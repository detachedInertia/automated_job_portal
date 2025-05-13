package com.project.automated_job_portal.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "invites")
@Data
public class InviteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private JobEntity job;

    private LocalDateTime invitedAt;

    @Column(name = "status", nullable = false)
    private String status; // Added status field

    @PrePersist
    protected void onCreate() {
        this.invitedAt = LocalDateTime.now();
        this.status = "PENDING"; // Set initial status to PENDING
    }
}