package com.project.automated_job_portal.repository;

import com.project.automated_job_portal.entity.JobEntity;
import com.project.automated_job_portal.entity.UserEntity;
import com.project.automated_job_portal.entity.WatchlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<WatchlistEntity, Long> {
    List<WatchlistEntity> findByUser(UserEntity user);
    Optional<WatchlistEntity> findByUserAndJob(UserEntity user, JobEntity job);
}