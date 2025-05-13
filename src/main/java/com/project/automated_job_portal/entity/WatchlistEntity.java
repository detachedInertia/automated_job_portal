package com.project.automated_job_portal.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "watchlist")
@Data
public class WatchlistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private JobEntity job;
}