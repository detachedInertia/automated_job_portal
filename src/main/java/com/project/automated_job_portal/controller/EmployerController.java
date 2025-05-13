package com.project.automated_job_portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/employer")
public class EmployerController {

    @GetMapping("/dashboard")
    public String employerDashboard() {
        return "employer-dashboard";
    }
}