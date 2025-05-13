package com.project.automated_job_portal.service;

import com.project.automated_job_portal.entity.ResumeEntity;
import com.project.automated_job_portal.entity.UserEntity;
import com.project.automated_job_portal.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public ResumeEntity getUserResume(UserEntity user) {
        return resumeRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElse(null);
    }

    public void saveResume(ResumeEntity resume) {
        resumeRepository.save(resume);
    }
}