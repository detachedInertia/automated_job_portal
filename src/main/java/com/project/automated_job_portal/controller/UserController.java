package com.project.automated_job_portal.controller;

import com.project.automated_job_portal.dto.ProfileRequest;
import com.project.automated_job_portal.entity.UserEntity;
import com.project.automated_job_portal.util.JwtUtil;
import com.project.automated_job_portal.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Controller
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/profile")
    @ResponseBody
    public ResponseEntity<UserEntity> getProfile(HttpServletRequest request) {
        String token = Arrays.stream(request.getCookies())
                .filter(cookie -> "jwtToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = jwtUtil.getUsernameFromToken(token);
        UserEntity user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("profileRequest") ProfileRequest profileRequest,
                                HttpServletRequest request, Model model) {
        try {
            String token = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwtToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
            if (token == null || !jwtUtil.validateToken(token)) {
                return "redirect:/login-email?error=Please log in to update your profile";
            }
            String email = jwtUtil.getUsernameFromToken(token);
            userService.updateProfile(email, profileRequest);
            model.addAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "profile";
    }
}