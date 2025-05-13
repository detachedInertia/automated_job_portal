package com.project.automated_job_portal.controller;

import com.project.automated_job_portal.entity.JobEntity;
import com.project.automated_job_portal.service.JobService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class SupportController {

    private final JobService jobService;

    public SupportController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/contact-us")
    public String contactUs() {
        return "contact-us"; // Create a contact-us.html template
    }

    @GetMapping("/help")
    public String help() {
        return "help"; // Create a help.html template
    }

    @GetMapping("/app-settings")
    public String appSettings() {
        return "app-settings"; // Create an app-settings.html template
    }

    @GetMapping("/jobs")
    public String jobs(Model model) {
        List<JobEntity> jobs = jobService.getAllJobs();
        model.addAttribute("jobs", jobs);
        return "jobs"; // Create a jobs.html template
    }
}