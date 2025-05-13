package com.project.automated_job_portal.service;

import com.project.automated_job_portal.entity.ApplicationEntity;
import com.project.automated_job_portal.entity.JobEntity;
import com.project.automated_job_portal.entity.ResumeEntity;
import com.project.automated_job_portal.entity.UserEntity;
import com.project.automated_job_portal.repository.ApplicationRepository;
import com.project.automated_job_portal.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserService userService;
    private final ResumeService resumeService;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository,
                              UserService userService, ResumeService resumeService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userService = userService;
        this.resumeService = resumeService;
    }

    public void applyForJob(Long jobId, String userEmail) {
        UserEntity user = userService.findByEmail(userEmail);
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        ResumeEntity resume = resumeService.getUserResume(user);
        if (resume == null) {
            throw new RuntimeException("No resume found. Please upload a resume first");
        }

        ApplicationEntity application = new ApplicationEntity();
        application.setUser(user);
        application.setJob(job);
        application.setStatus("Pending");
        application.setResume(resume);

        applicationRepository.save(application);
    }

    public void saveApplication(ApplicationEntity application) {
        applicationRepository.save(application);
    }

    public List<ApplicationEntity> getApplicationsByUserEmail(String userEmail) {
        UserEntity user = userService.findByEmail(userEmail);
        return applicationRepository.findByUser(user);
    }

    public void updateApplicationStatus(Long applicationId, String status) {
        ApplicationEntity application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));
        application.setStatus(status);
        applicationRepository.save(application);
    }
}