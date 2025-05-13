package com.project.automated_job_portal.controller;

import com.project.automated_job_portal.entity.UserEntity;
import com.project.automated_job_portal.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/complete-profile")
    public String showProfileForm(HttpSession session, Model model) {
        String email = (String) session.getAttribute("oauth_email");
        if (email == null) {
            return "redirect:/login-email";
        }
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFirstName((String) session.getAttribute("oauth_firstName"));
        model.addAttribute("user", user);
        return "complete-profile";
    }

    @PostMapping("/complete-profile")
    public String completeProfile(@ModelAttribute UserEntity user, HttpSession session, Model model) {
        try {
            String email = (String) session.getAttribute("oauth_email");
            user.setEmail(email);
            userService.saveOAuthUser(user);
            session.removeAttribute("oauth_email");
            session.removeAttribute("oauth_firstName");
            return "redirect:/auth/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "complete-profile";
        }
    }
}