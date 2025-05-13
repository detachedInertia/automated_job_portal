package com.project.automated_job_portal.service;

import com.project.automated_job_portal.entity.JobEntity;
import com.project.automated_job_portal.entity.UserEntity;
import com.project.automated_job_portal.entity.WatchlistEntity;
import com.project.automated_job_portal.repository.JobRepository;
import com.project.automated_job_portal.repository.WatchlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final JobRepository jobRepository;
    private final UserService userService;

    public WatchlistService(WatchlistRepository watchlistRepository, JobRepository jobRepository, UserService userService) {
        this.watchlistRepository = watchlistRepository;
        this.jobRepository = jobRepository;
        this.userService = userService;
    }

    public void addToWatchlist(Long jobId, String userEmail) {
        UserEntity user = userService.findByEmail(userEmail);
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        // Check for duplicate entry
        if (watchlistRepository.findByUserAndJob(user, job).isPresent()) {
            throw new RuntimeException("Job already in watchlist");
        }

        WatchlistEntity watchlistEntry = new WatchlistEntity();
        watchlistEntry.setUser(user);
        watchlistEntry.setJob(job);

        watchlistRepository.save(watchlistEntry);
    }

    public List<WatchlistEntity> getWatchlistByUserEmail(String userEmail) {
        UserEntity user = userService.findByEmail(userEmail);
        return watchlistRepository.findByUser(user);
    }
}