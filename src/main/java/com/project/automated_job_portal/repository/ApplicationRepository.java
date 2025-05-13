package com.project.automated_job_portal.repository;

import com.project.automated_job_portal.entity.ApplicationEntity;
import com.project.automated_job_portal.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {
    List<ApplicationEntity> findByUser(UserEntity user);
}