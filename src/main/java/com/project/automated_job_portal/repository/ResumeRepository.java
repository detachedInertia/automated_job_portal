package com.project.automated_job_portal.repository;

import com.project.automated_job_portal.entity.ResumeEntity;
import com.project.automated_job_portal.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {
    Optional<ResumeEntity> findTopByUserOrderByCreatedAtDesc(UserEntity user);
}