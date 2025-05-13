package com.project.automated_job_portal.controller;

import com.project.automated_job_portal.dto.*;
import com.project.automated_job_portal.entity.*;
import com.project.automated_job_portal.service.*;
import com.project.automated_job_portal.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final JwtUtil jwtUtil;
    private final WatchlistService watchlistService;
    private final InviteService inviteService;
    private final ResumeService resumeService;

    public AuthController(UserService userService, JobService jobService,
                          ApplicationService applicationService, JwtUtil jwtUtil,
                          WatchlistService watchlistService, InviteService inviteService,
                          ResumeService resumeService) {
        this.userService = userService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.jwtUtil = jwtUtil;
        this.watchlistService = watchlistService;
        this.inviteService = inviteService;
        this.resumeService = resumeService;
    }

    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/auth/otp-request?error=Please log in to access the dashboard";
        }

        String userEmail = jwtUtil.getUsernameFromToken(token);
        UserEntity user;
        try {
            user = userService.findByEmail(userEmail);
        } catch (RuntimeException e) {
            model.addAttribute("error", "User not found");
            return "dashboard";
        }

        UserEntity userWithEducation = userService.findByIdWithEducation(user.getId());
        if (userWithEducation != null) {
            model.addAttribute("user", userWithEducation);
        } else {
            model.addAttribute("error", "User not found");
        }

        List<String> roles = jwtUtil.getRolesFromToken(token);
        model.addAttribute("roles", roles.isEmpty() ? Collections.singletonList("USER") : roles);

        List<ApplicationEntity> applications = applicationService.getApplicationsByUserEmail(userEmail);
        model.addAttribute("applications", applications);

        model.addAttribute("jobs", jobService.getAllJobs());
        model.addAttribute("internships", jobService.getAllInternships());
        return "dashboard";
    }

    @GetMapping("/otp-request")
    public String showOtpRequestForm(Model model) {
        model.addAttribute("otpRequest", new OtpRequest());
        return "otp-request";
    }

    @PostMapping("/request-otp")
    public String requestOtp(@Valid @ModelAttribute("otpRequest") OtpRequest otpRequest,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "otp-request";
        }
        try {
            userService.requestOtp(otpRequest);
            model.addAttribute("message", "OTP sent to " + otpRequest.getIdentifier());
            model.addAttribute("otpVerificationRequest", new OtpVerificationRequest(otpRequest.getIdentifier()));
            model.addAttribute("identifier", otpRequest.getIdentifier());
            return "otp-verify";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "otp-request";
        }
    }

    @GetMapping("/apply-with-existing/{jobId}")
    public String applyWithExistingResume(@PathVariable Long jobId, HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to apply for a job";
        }

        String userEmail = jwtUtil.getUsernameFromToken(token);
        UserEntity user = userService.findByEmail(userEmail);
        if (user == null) {
            return "redirect:/auth/dashboard?error=User not found";
        }

        JobEntity job = jobService.findById(jobId);
        if (job == null) {
            return "redirect:/auth/dashboard?error=Job not found";
        }

        ResumeEntity resume = resumeService.getUserResume(user);
        if (resume == null) {
            return "redirect:/auth/dashboard?error=No resume found. Please upload a resume first";
        }

        ApplicationEntity application = new ApplicationEntity();
        application.setUser(user);
        application.setJob(job);
        application.setAppliedAt(LocalDateTime.now());
        application.setStatus("Pending");
        application.setResume(resume);

        try {
            applicationService.saveApplication(application);
            return "redirect:/auth/dashboard?success=Application submitted successfully";
        } catch (Exception e) {
            return "redirect:/auth/dashboard?error=Failed to submit application: " + e.getMessage();
        }
    }

    @GetMapping("/apply-with-existing/internship/{internshipId}")
    public String applyWithExistingResumeForInternship(@PathVariable Long internshipId, HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to apply for an internship";
        }

        String userEmail = jwtUtil.getUsernameFromToken(token);
        UserEntity user = userService.findByEmail(userEmail);
        if (user == null) {
            return "redirect:/auth/dashboard?error=User not found";
        }

        JobEntity internship = jobService.findInternshipById(internshipId);
        if (internship == null) {
            return "redirect:/auth/dashboard?error=Internship not found";
        }

        ResumeEntity resume = resumeService.getUserResume(user);
        if (resume == null) {
            return "redirect:/auth/dashboard?error=No resume found. Please upload a resume first";
        }

        ApplicationEntity application = new ApplicationEntity();
        application.setUser(user);
        application.setJob(internship);
        application.setAppliedAt(LocalDateTime.now());
        application.setStatus("Pending");
        application.setResume(resume);

        try {
            applicationService.saveApplication(application);
            return "redirect:/auth/dashboard?success=Internship application submitted successfully";
        } catch (Exception e) {
            return "redirect:/auth/dashboard?error=Failed to submit internship application: " + e.getMessage();
        }
    }

    @PostMapping("/apply-with-custom/{jobId}")
    public String applyWithCustomResume(@PathVariable Long jobId, @RequestParam String customResume, HttpServletRequest request) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to apply for a job";
        }

        String userEmail = jwtUtil.getUsernameFromToken(token);
        UserEntity user = userService.findByEmail(userEmail);
        if (user == null) {
            return "redirect:/auth/dashboard?error=User not found";
        }

        JobEntity job = jobService.findById(jobId);
        if (job == null) {
            return "redirect:/auth/dashboard?error=Job not found";
        }

        ResumeEntity resume = new ResumeEntity();
        resume.setUser(user);
        resume.setContent(customResume);
        resume.setCreatedAt(LocalDateTime.now());

        try {
            resumeService.saveResume(resume);

            ApplicationEntity application = new ApplicationEntity();
            application.setUser(user);
            application.setJob(job);
            application.setAppliedAt(LocalDateTime.now());
            application.setStatus("Pending");
            application.setResume(resume);

            applicationService.saveApplication(application);
            return "redirect:/auth/dashboard?success=Application submitted successfully with custom resume";
        } catch (Exception e) {
            return "redirect:/auth/dashboard?error=Failed to submit application: " + e.getMessage();
        }
    }

    @PostMapping("/apply-with-custom/internship/{internshipId}")
    public String applyWithCustomResumeForInternship(@PathVariable Long internshipId, @RequestParam String customResume, HttpServletRequest request) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to apply for an internship";
        }

        String userEmail = jwtUtil.getUsernameFromToken(token);
        UserEntity user = userService.findByEmail(userEmail);
        if (user == null) {
            return "redirect:/auth/dashboard?error=User not found";
        }

        JobEntity internship = jobService.findInternshipById(internshipId);
        if (internship == null) {
            return "redirect:/auth/dashboard?error=Internship not found";
        }

        ResumeEntity resume = new ResumeEntity();
        resume.setUser(user);
        resume.setContent(customResume);
        resume.setCreatedAt(LocalDateTime.now());

        try {
            resumeService.saveResume(resume);

            ApplicationEntity application = new ApplicationEntity();
            application.setUser(user);
            application.setJob(internship);
            application.setAppliedAt(LocalDateTime.now());
            application.setStatus("Pending");
            application.setResume(resume);

            applicationService.saveApplication(application);
            return "redirect:/auth/dashboard?success=Internship application submitted successfully with custom resume";
        } catch (Exception e) {
            return "redirect:/auth/dashboard?error=Failed to submit internship application: " + e.getMessage();
        }
    }

    @PostMapping("/apply/internship/{internshipId}")
    public String applyForInternship(@PathVariable Long internshipId, HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String userEmail = jwtUtil.getUsernameFromToken(token);
            try {
                applicationService.applyForJob(internshipId, userEmail);
                model.addAttribute("success", "Successfully applied for the internship!");
            } catch (Exception e) {
                model.addAttribute("error", "Failed to apply for the internship: " + e.getMessage());
            }
            return "redirect:/auth/dashboard";
        }
        return "redirect:/login-email?error=Please log in to apply for an internship";
    }

    @GetMapping("/jobs-applied")
    public String jobsApplied(HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to view your applied jobs";
        }
        String userEmail = jwtUtil.getUsernameFromToken(token);
        List<ApplicationEntity> applications = applicationService.getApplicationsByUserEmail(userEmail);
        model.addAttribute("applications", applications);
        return "jobs-applied";
    }

    @Transactional
    @PostMapping("/verify-otp")
    public String verifyOtp(@Valid @ModelAttribute("otpVerificationRequest") OtpVerificationRequest verificationRequest,
                            BindingResult result, HttpServletResponse response, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("message", "Please enter the OTP sent to " + verificationRequest.getIdentifier());
            model.addAttribute("identifier", verificationRequest.getIdentifier());
            return "otp-verify";
        }
        try {
            String token = userService.verifyOtp(verificationRequest);
            Cookie jwtCookie = new Cookie("jwtToken", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(86400); // Updated to 24 hours
            response.addCookie(jwtCookie);
            return "redirect:/auth/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("message", "Please enter the OTP sent to " + verificationRequest.getIdentifier());
            model.addAttribute("identifier", verificationRequest.getIdentifier());
            return "otp-verify";
        }
    }

    @GetMapping("/watchlist")
    public String watchlist(HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            String redirectTo = URLEncoder.encode("/auth/watchlist", StandardCharsets.UTF_8);
            return "redirect:/login-email?error=Please log in to view your watchlist&redirectTo=" + redirectTo;
        }
        String userEmail = jwtUtil.getUsernameFromToken(token);
        List<WatchlistEntity> watchlist = watchlistService.getWatchlistByUserEmail(userEmail);
        model.addAttribute("watchlist", watchlist);
        return "watchlist";
    }

    @GetMapping("/otp-login")
    public String otpLoginForm(Model model) {
        model.addAttribute("otpRequest", new OtpRequest());
        return "redirect:/auth/otp-request";
    }

    @GetMapping("/login-success")
    public String loginSuccess(HttpServletRequest request, Model model,
                               @RequestParam(value = "redirectTo", required = false) String redirectTo) {
        String token = getJwtTokenFromCookies(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.getUsernameFromToken(token);
            List<String> roles = jwtUtil.getRolesFromToken(token);
            UserEntity user = userService.findByEmail(email);
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("roles", roles.isEmpty() ? Collections.singletonList(user.getRole()) : roles);
                if (user.getFirstName() == null || user.getSkills().isEmpty()) {
                    model.addAttribute("profileRequest", new ProfileRequest());
                    model.addAttribute("redirectTo", redirectTo);
                    return "registration";
                }
                return redirectTo != null && !redirectTo.isEmpty() ?
                        "redirect:" + redirectTo : "redirect:/auth/dashboard";
            }
        }
        String redirectParam = redirectTo != null && !redirectTo.isEmpty() ?
                "&redirectTo=" + URLEncoder.encode(redirectTo, StandardCharsets.UTF_8) : "";
        return "redirect:/login-email?error=Invalid login" + redirectParam;
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("profileRequest") ProfileRequest profileRequest,
                           BindingResult result, HttpServletResponse response, Model model) {
        if (result.hasErrors()) {
            return "registration";
        }
        try {
            String token = userService.registerUser(profileRequest);
            Cookie jwtCookie = new Cookie("jwtToken", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(86400); // Updated to 24 hours
            response.addCookie(jwtCookie);
            return "redirect:/auth/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "registration";
        }
    }

    @PostMapping("/update-profile")
    public String updateProfile(@Valid @ModelAttribute("profileRequest") ProfileRequest profileRequest,
                                BindingResult result, HttpServletRequest request, Model model,
                                @RequestParam(value = "redirectTo", required = false) String redirectTo) {
        if (result.hasErrors()) {
            model.addAttribute("redirectTo", redirectTo);
            return "registration";
        }
        try {
            String token = getJwtTokenFromCookies(request);
            if (token != null && jwtUtil.validateToken(token)) {
                String email = jwtUtil.getUsernameFromToken(token);
                userService.updateProfileAfterRegistration(email, profileRequest);
                return redirectTo != null && !redirectTo.isEmpty() ?
                        "redirect:" + redirectTo : "redirect:/auth/dashboard";
            }
            String redirectParam = redirectTo != null && !redirectTo.isEmpty() ?
                    "&redirectTo=" + URLEncoder.encode(redirectTo, StandardCharsets.UTF_8) : "";
            return "redirect:/login-email?error=Invalid session" + redirectParam;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("redirectTo", redirectTo);
            return "registration";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("email", "");
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        try {
            userService.sendPasswordResetLink(email);
            model.addAttribute("message", "Password reset link sent to " + email);
        } catch (Exception e) {
            model.addAttribute("error", "No user found with email: " + email);
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        try {
            userService.validateResetToken(token);
            model.addAttribute("token", token);
            model.addAttribute("newPassword", "");
            return "reset-password";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "forgot-password";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token, @RequestParam String newPassword, Model model) {
        try {
            userService.resetPassword(token, newPassword);
            model.addAttribute("message", "Password reset successfully. Please log in.");
            return "login-email";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        }
    }

    @PostMapping("/apply/{jobId}")
    public String applyForJob(@PathVariable Long jobId, HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String userEmail = jwtUtil.getUsernameFromToken(token);
            try {
                applicationService.applyForJob(jobId, userEmail);
                model.addAttribute("success", "Successfully applied for the job!");
            } catch (Exception e) {
                model.addAttribute("error", "Failed to apply for the job: " + e.getMessage());
            }
            return "redirect:/auth/dashboard";
        }
        return "redirect:/login-email?error=Please log in to apply for a job";
    }

    @GetMapping("/complete-profile")
    public String showCompleteProfileForm(Model model, HttpServletRequest request) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to complete your profile";
        }
        model.addAttribute("profileRequest", new ProfileRequest());
        return "registration";
    }

    @PostMapping("/complete-profile")
    public String completeProfile(@Valid @ModelAttribute("profileRequest") ProfileRequest profileRequest,
                                  BindingResult result, HttpServletRequest request, HttpServletResponse response, Model model) {
        if (result.hasErrors()) {
            return "registration";
        }
        try {
            String token = getJwtTokenFromCookies(request);
            if (token == null || !jwtUtil.validateToken(token)) {
                return "redirect:/login-email?error=Invalid session";
            }
            String email = jwtUtil.getUsernameFromToken(token);
            userService.updateProfileAfterRegistration(email, profileRequest);
            String newToken = userService.loginWithForm(email);
            Cookie jwtCookie = new Cookie("jwtToken", newToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(86400); // Updated to 24 hours
            response.addCookie(jwtCookie);
            return "redirect:/auth/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to complete profile: " + e.getMessage());
            return "registration";
        }
    }

    @PostMapping("/form-login")
    public String formLogin(@RequestParam String email, @RequestParam String password,
                            @RequestParam(value = "redirectTo", required = false) String redirectTo,
                            HttpServletResponse response, Model model) {
        try {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail(email);
            loginRequest.setPassword(password);
            String token = userService.login(loginRequest);
            Cookie jwtCookie = new Cookie("jwtToken", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(86400); // Updated to 24 hours
            response.addCookie(jwtCookie);
            String redirectParam = redirectTo != null && !redirectTo.isEmpty() ?
                    "?redirectTo=" + URLEncoder.encode(redirectTo, StandardCharsets.UTF_8) : "";
            return "redirect:/auth/login-success" + redirectParam;
        } catch (Exception e) {
            model.addAttribute("error", "Invalid email/phone or password");
            model.addAttribute("redirectTo", redirectTo);
            return "login-email";
        }
    }

    @GetMapping("/resume-builder")
    public String resumeBuilder(HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to access the resume builder";
        }
        String email = jwtUtil.getUsernameFromToken(token);
        UserEntity user = userService.findByEmail(email);
        if (user == null) {
            return "redirect:/login-email?error=User not found";
        }
        if (user.getFirstName() == null || user.getSkills().isEmpty()) {
            model.addAttribute("profileRequest", new ProfileRequest());
            model.addAttribute("resumeBuilder", true);
            return "registration";
        }
        model.addAttribute("user", user);
        return "resume";
    }

    @GetMapping("/profile")
    public String viewProfile(HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/profile?error=Please log in to view your profile";
        }
        String email = jwtUtil.getUsernameFromToken(token);
        UserEntity user = userService.findByEmail(email);
        if (user == null) {
            return "redirect:/profile?error=User not found";
        }

        Hibernate.initialize(user.getSkills());
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/apply")
    public String apply(HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to apply for jobs";
        }
        String userEmail = jwtUtil.getUsernameFromToken(token);
        model.addAttribute("jobs", jobService.getAllJobs());
        model.addAttribute("internships", jobService.getAllInternships());
        return "apply";
    }

    @GetMapping("/invites")
    public String invites(HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            String redirectTo = URLEncoder.encode("/auth/invites", StandardCharsets.UTF_8);
            return "redirect:/login-email?error=Please log in to view invites&redirectTo=" + redirectTo;
        }
        String email = jwtUtil.getUsernameFromToken(token);
        List<InviteEntity> invites = inviteService.getInvitesByUserEmail(email);
        model.addAttribute("invites", invites);
        return "invites";
    }

    @GetMapping("/settings")
    public String settings(HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to access settings";
        }
        String email = jwtUtil.getUsernameFromToken(token);
        UserEntity user = userService.findByEmail(email);
        if (user == null) {
            return "redirect:/login-email?error=User not found";
        }
        model.addAttribute("user", user);
        return "settings";
    }

    @GetMapping("/resume")
    public String resume(HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to view your resume";
        }
        String email = jwtUtil.getUsernameFromToken(token);
        UserEntity user = userService.findByEmail(email);
        if (user == null) {
            return "redirect:/login-email?error=User not found";
        }
        ResumeEntity resume = resumeService.getUserResume(user);
        model.addAttribute("resume", resume);
        return "resume";
    }

    @GetMapping("/update-profile")
    public String getUpdateProfile(HttpServletRequest request) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to update your profile";
        }
        return "update-profile";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwtToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login-email?message=Successfully logged out";
    }

    @GetMapping("/contact-us")
    public String showContactUsForm(Model model) {
        model.addAttribute("name", "");
        model.addAttribute("email", "");
        model.addAttribute("message", "");
        return "contact-us";
    }

    @PostMapping("/contact-us")
    public String processContactUsForm(@RequestParam String name, @RequestParam String email,
                                       @RequestParam String message, Model model) {
        model.addAttribute("message", "Thank you, " + name + "! Your message has been received.");
        return "contact-us";
    }

    @GetMapping("/help")
    public String showHelpPage() {
        return "help";
    }

    @GetMapping("/app-settings")
    public String showAppSettings(HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to access app settings";
        }
        String email = jwtUtil.getUsernameFromToken(token);
        UserEntity user = userService.findByEmail(email);
        if (user == null) {
            return "redirect:/login-email?error=User not found";
        }
        model.addAttribute("emailNotifications", "enabled");
        model.addAttribute("theme", "dark");
        return "app-settings";
    }

    @PostMapping("/app-settings")
    public String saveAppSettings(@RequestParam String emailNotifications, @RequestParam String theme,
                                  HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to save app settings";
        }
        model.addAttribute("message", "Settings saved successfully!");
        return "app-settings";
    }

    @GetMapping("/jobs")
    public String showJobs(HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to view jobs";
        }
        model.addAttribute("jobs", jobService.getAllJobs());
        model.addAttribute("internships", jobService.getAllInternships());
        return "jobs";
    }

    @PostMapping("/watchlist/add/{jobId}")
    public String addToWatchlist(@PathVariable Long jobId, HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to add to watchlist";
        }
        String userEmail = jwtUtil.getUsernameFromToken(token);
        try {
            watchlistService.addToWatchlist(jobId, userEmail);
            model.addAttribute("message", "Job added to watchlist!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add to watchlist: " + e.getMessage());
        }
        return "redirect:/auth/jobs";
    }

    @PostMapping("/invites/accept/{inviteId}")
    public String acceptInvite(@PathVariable Long inviteId, HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to accept invites";
        }
        try {
            inviteService.acceptInvite(inviteId);
            model.addAttribute("message", "Invite accepted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to accept invite: " + e.getMessage());
        }
        return "redirect:/auth/invites";
    }

    @PostMapping("/invites/decline/{inviteId}")
    public String declineInvite(@PathVariable Long inviteId, HttpServletRequest request, Model model) {
        String token = getJwtTokenFromCookies(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login-email?error=Please log in to decline invites";
        }
        try {
            inviteService.declineInvite(inviteId);
            model.addAttribute("message", "Invite declined successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to decline invite: " + e.getMessage());
        }
        return "redirect:/auth/invites";
    }

    private String getJwtTokenFromCookies(HttpServletRequest request) {
        String token = request.getCookies() != null ?
                Arrays.stream(request.getCookies())
                        .filter(cookie -> "jwtToken".equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null) : null;
        if (token == null) {
            System.out.println("No JWT token found in cookies");
        }
        return token;
    }
}