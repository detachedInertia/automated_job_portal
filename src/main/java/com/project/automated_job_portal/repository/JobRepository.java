package com.project.automated_job_portal.repository;

import com.project.automated_job_portal.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {
    List<JobEntity> findAllByOrderByCreatedAtDesc();

    @Query("SELECT j FROM JobEntity j WHERE j.type = :type")
    List<JobEntity> findAllByType(@Param("type") String type);

    @Query("SELECT j FROM JobEntity j WHERE j.id = :id AND j.type = :type")
    Optional<JobEntity> findByIdAndType(@Param("id") Long id, @Param("type") String type);
}