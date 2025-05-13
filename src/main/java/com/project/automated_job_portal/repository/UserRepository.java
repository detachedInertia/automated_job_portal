package com.project.automated_job_portal.repository;

import com.project.automated_job_portal.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByPhone(String phone);

    Optional<UserEntity> findByPhoneIgnoreCase(String phone);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.educationHistory WHERE u.id = :id")
    Optional<UserEntity> findByIdWithEducation(@Param("id") Long id);
}