package com.project.automated_job_portal.repository;

import com.project.automated_job_portal.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {
    Optional<OtpEntity> findByIdentifierAndOtp(String identifier, String otp);
    void deleteByIdentifier(String identifier);
}