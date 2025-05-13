package com.project.automated_job_portal.controller;

import com.project.automated_job_portal.dto.SignupRequest;
import com.project.automated_job_portal.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {

    private final UserService userService;

    public SignupController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute("signupRequest") SignupRequest signupRequest, Model model) {
        try {
            userService.signup(signupRequest);
            // Redirect to login-email with the email pre-filled
            return "redirect:/login-email?signupSuccess=true&email=" + signupRequest.getEmail();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }
}