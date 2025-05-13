package com.project.automated_job_portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/login-email")
    public String loginEmail() {
        return "login-email"; // Renders the login-email.html Thymeleaf template
    }
}