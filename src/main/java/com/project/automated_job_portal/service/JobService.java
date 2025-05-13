package com.project.automated_job_portal.service;

import com.project.automated_job_portal.config.JobNotificationHandler;
import com.project.automated_job_portal.dto.JobPostingRequest;
import com.project.automated_job_portal.entity.JobEntity;
import com.project.automated_job_portal.entity.UserEntity;
import com.project.automated_job_portal.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final UserService userService;
    private final JobNotificationHandler jobNotificationHandler;

    public JobService(JobRepository jobRepository, UserService userService, JobNotificationHandler jobNotificationHandler) {
        this.jobRepository = jobRepository;
        this.userService = userService;
        this.jobNotificationHandler = jobNotificationHandler;
    }

    public List<JobEntity> getAllJobs() {
        return jobRepository.findAllByType("JOB");
    }

    public List<JobEntity> getAllInternships() {
        return jobRepository.findAllByType("INTERNSHIP");
    }

    public JobEntity findById(Long id) {
        return jobRepository.findByIdAndType(id, "JOB")
                .orElse(null);
    }

    public JobEntity findInternshipById(Long id) {
        return jobRepository.findByIdAndType(id, "INTERNSHIP")
                .orElse(null);
    }

    public void createJob(JobPostingRequest request, String userEmail) {
        UserEntity user = userService.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!"EMPLOYER".equals(user.getRole())) {
            throw new RuntimeException("Only employers can post jobs");
        }

        JobEntity job = new JobEntity();
        job.setTitle(request.getTitle());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setDescription(request.getDescription());
        job.setSalary(request.getSalary());
        job.setPostedBy(user);

        jobRepository.save(job);

        // Broadcast the new job to all connected WebSocket clients
        try {
            jobNotificationHandler.broadcastJobNotification(job);
        } catch (Exception e) {
            logger.error("Failed to broadcast job notification: {}", e.getMessage(), e);
        }
    }
}