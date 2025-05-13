package com.project.automated_job_portal.service;

import com.project.automated_job_portal.entity.InviteEntity;
import com.project.automated_job_portal.entity.JobEntity;
import com.project.automated_job_portal.entity.UserEntity;
import com.project.automated_job_portal.repository.InviteRepository;
import com.project.automated_job_portal.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InviteService {

    private final InviteRepository inviteRepository;
    private final UserService userService;
    private final JobRepository jobRepository;

    public InviteService(InviteRepository inviteRepository, UserService userService, JobRepository jobRepository) {
        this.inviteRepository = inviteRepository;
        this.userService = userService;
        this.jobRepository = jobRepository;
    }

    public List<InviteEntity> getInvitesByUserEmail(String userEmail) {
        UserEntity user = userService.findByEmail(userEmail);
        return inviteRepository.findByUser(user);
    }

    public void createInvite(Long jobId, String userEmail, String invitedByEmail) {
        UserEntity user = userService.findByEmail(userEmail);
        UserEntity invitedBy = userService.findByEmail(invitedByEmail);
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        InviteEntity invite = new InviteEntity();
        invite.setUser(user);
        invite.setJob(job);
        invite.setInvitedAt(LocalDateTime.now());
        invite.setStatus("PENDING"); // Set initial status to PENDING
        inviteRepository.save(invite);
    }

    @Transactional
    public void acceptInvite(Long inviteId) {
        Optional<InviteEntity> inviteOptional = inviteRepository.findById(inviteId);
        if (inviteOptional.isPresent()) {
            InviteEntity invite = inviteOptional.get();
            if ("PENDING".equals(invite.getStatus())) {
                invite.setStatus("ACCEPTED");
                inviteRepository.save(invite);
            } else {
                throw new IllegalStateException("Invite is not in a pending state.");
            }
        } else {
            throw new RuntimeException("Invite not found with id: " + inviteId);
        }
    }

    @Transactional
    public void declineInvite(Long inviteId) {
        Optional<InviteEntity> inviteOptional = inviteRepository.findById(inviteId);
        if (inviteOptional.isPresent()) {
            InviteEntity invite = inviteOptional.get();
            if ("PENDING".equals(invite.getStatus())) {
                invite.setStatus("DECLINED");
                inviteRepository.save(invite);
            } else {
                throw new IllegalStateException("Invite is not in a pending state.");
            }
        } else {
            throw new RuntimeException("Invite not found with id: " + inviteId);
        }
    }
}