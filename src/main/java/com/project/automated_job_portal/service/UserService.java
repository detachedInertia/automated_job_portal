package com.project.automated_job_portal.service;

import com.project.automated_job_portal.config.JobNotificationHandler;
import com.project.automated_job_portal.dto.*;
import com.project.automated_job_portal.entity.PasswordResetToken;
import com.project.automated_job_portal.repository.PasswordResetTokenRepository;
import com.project.automated_job_portal.entity.EducationEntity;
import com.project.automated_job_portal.entity.OtpEntity;
import com.project.automated_job_portal.entity.UserEntity;
import com.project.automated_job_portal.entity.WorkExperienceEntity;
import com.project.automated_job_portal.repository.OtpRepository;
import com.project.automated_job_portal.repository.UserRepository;
import com.project.automated_job_portal.util.JwtUtil;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @Value("${file.upload-dir:uploads/resumes/}")
    private String uploadDir;

    public UserService(UserRepository userRepository, OtpRepository otpRepository,
                       PasswordResetTokenRepository tokenRepository, JwtUtil jwtUtil,
                       JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.tokenRepository = tokenRepository;
        this.jwtUtil = jwtUtil;
        this.mailSender = mailSender;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getRole() != null ?
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())) :
                        Collections.emptyList()
        );
    }

    @PostConstruct
    public void logTwilioConfig() {
        logger.debug("Twilio Config - SID: {}, Phone: {}",
                twilioAccountSid != null && !twilioAccountSid.isEmpty() ? twilioAccountSid.substring(0, 4) + "..." : "null",
                twilioPhoneNumber != null && !twilioPhoneNumber.isEmpty() ? twilioPhoneNumber : "null");
    }

    @Transactional
    public String signup(SignupRequest signupRequest) {
        String email = signupRequest.getEmail().toLowerCase();
        String phone = signupRequest.getPhone().toLowerCase();
        logger.info("Attempting to sign up user with email: {} and phone: {}", email, phone);

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            logger.warn("Email already exists: {}", email);
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.findByPhoneIgnoreCase(phone).isPresent()) {
            logger.warn("Phone number already exists: {}", phone);
            throw new RuntimeException("Phone number already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(signupRequest.getRole() != null ? signupRequest.getRole() : "USER");

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        logger.info("JWT token generated successfully for user: {}", email);

        logger.info("Saving new user with email: {} and phone: {}", email, phone);
        userRepository.save(user);

        return token;
    }

    public String login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().toLowerCase();
        logger.info("Attempting to log in user with email: {}", email);

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> userRepository.findByPhoneIgnoreCase(email)
                        .orElseThrow(() -> {
                            logger.warn("Invalid email or phone: {}", email);
                            return new RuntimeException("Invalid email/phone or password");
                        }));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Invalid password for email/phone: {}", email);
            throw new RuntimeException("Invalid email/phone or password");
        }

        logger.info("Login successful for user: {}", email);
        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }

    public String loginWithForm(String email) {
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new RuntimeException("User not found with email: " + email);
                });
        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.toLowerCase()).isPresent();
    }

    public UserEntity findByEmail(String email) {
        String normalizedEmail = email.toLowerCase();
        logger.info("Finding user by email: {}", normalizedEmail);
        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", normalizedEmail);
                    return new RuntimeException("User not found with email: " + email);
                });
    }

    public UserEntity findByPhoneNumber(String phone) {
        String normalizedPhone = phone.toLowerCase();
        logger.info("Finding user by phone: {}", normalizedPhone);
        return userRepository.findByPhoneIgnoreCase(normalizedPhone)
                .orElseThrow(() -> {
                    logger.warn("User not found with phone: {}", normalizedPhone);
                    return new RuntimeException("User not found with phone: " + phone);
                });
    }

    @Transactional
    public void updateProfile(String email, ProfileRequest profileRequest) throws IOException {
        updateUserProfile(email, profileRequest);
    }

    @Transactional
    public void requestOtp(OtpRequest otpRequest) {
        String identifier = otpRequest.getIdentifier().toLowerCase();
        logger.debug("Requesting OTP for identifier: {}", identifier);
        String otpItself = String.format("%06d", random.nextInt(999999));
        logger.info("Generated OTP {} for {}", otpItself, identifier);

        otpRepository.deleteByIdentifier(identifier); // Clear old OTP
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setIdentifier(identifier);
        otpEntity.setOtp(otpItself);
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otpEntity);

        if (identifier.contains("@")) {
            sendEmailOtp(identifier, otpItself);
        } else {
            sendSmsOtp(identifier, otpItself);
        }
    }

    private void sendEmailOtp(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Job Portal OTP");
        message.setText("Your OTP is: " + otp + "\nValid for 5 minutes.");
        mailSender.send(message);
        logger.info("OTP sent to email {}", email);
    }

    private void sendSmsOtp(String phone, String otp) {
        if (twilioAccountSid == null || twilioAccountSid.isEmpty() ||
                twilioAuthToken == null || twilioAuthToken.isEmpty() ||
                twilioPhoneNumber == null || twilioPhoneNumber.isEmpty()) {
            logger.error("Twilio configuration missing: SID={}, Phone={}",
                    twilioAccountSid, twilioPhoneNumber);
            throw new RuntimeException("SMS OTP service not configured");
        }
        try {
            logger.debug("Initializing Twilio with SID: {}...",
                    twilioAccountSid.substring(0, 4));
            Twilio.init(twilioAccountSid, twilioAuthToken);
            Message message = Message.creator(
                    new PhoneNumber(phone),
                    new PhoneNumber(twilioPhoneNumber),
                    "Your Job Portal OTP is: " + otp + ". Valid for 5 minutes."
            ).create();
            logger.info("SMS OTP sent to {}, SID: {}", phone, message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send SMS OTP to {}: {}", phone, e.getMessage());
            throw new RuntimeException("Failed to send SMS OTP: " + e.getMessage());
        }
    }

    @Transactional
    public String verifyOtp(OtpVerificationRequest verificationRequest) {
        logger.info("Transaction active: {}", TransactionSynchronizationManager.isActualTransactionActive());
        String identifier = verificationRequest.getIdentifier().toLowerCase();
        String otp = verificationRequest.getOtp();

        OtpEntity otpEntity = otpRepository.findByIdentifierAndOtp(identifier, otp)
                .orElseThrow(() -> {
                    logger.warn("Invalid OTP for {}", identifier);
                    return new RuntimeException("Invalid OTP");
                });

        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpEntity);
            logger.warn("Expired OTP for {}", identifier);
            throw new RuntimeException("OTP has expired");
        }

        UserEntity user = userRepository.findByEmailIgnoreCase(identifier)
                .orElseGet(() -> userRepository.findByPhoneIgnoreCase(identifier)
                        .orElse(null)); // Allow null for new user

        if (user == null) {
            // Register new user
            user = new UserEntity();
            if (identifier.contains("@")) {
                user.setEmail(identifier);
            } else {
                user.setPhone(identifier);
            }
            user.setPassword(passwordEncoder.encode(String.format("%06d", random.nextInt(999999)))); // Random password
            user.setRole("USER");
            userRepository.save(user);
            logger.info("New user registered with identifier: {}", identifier);
        }

        otpRepository.delete(otpEntity);
        String token = jwtUtil.generateToken(user.getEmail() != null ? user.getEmail() : user.getPhone(), user.getRole());
        logger.info("OTP verified, token issued for {}", identifier);
        return token;
    }

    @Transactional(readOnly = true)
    public UserEntity findByIdWithEducation(Long id) {
        logger.info("Finding user by ID with education history: {}", id);
        Optional<UserEntity> userOptional = userRepository.findByIdWithEducation(id);
        return userOptional.orElseThrow(() -> {
            logger.warn("User not found with ID: {}", id);
            return new RuntimeException("User not found with ID: " + id);
        });
    }

    @Transactional
    public String loginWithOAuth(String email, String username) {
        String normalizedEmail = email != null ? email.toLowerCase() : (username + "@oauth.placeholder");
        logger.info("Attempting OAuth login/signup for email: {}", normalizedEmail);

        Optional<UserEntity> userOptional = userRepository.findByEmailIgnoreCase(normalizedEmail);
        UserEntity user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            logger.info("Existing user found for OAuth login: {}", normalizedEmail);
        } else {
            user = new UserEntity();
            user.setEmail(normalizedEmail);
            user.setFirstName(username);
            user.setPassword(passwordEncoder.encode("oauth-" + username + "-" + System.currentTimeMillis()));
            user.setRole("USER");
            userRepository.save(user);
            logger.info("New user signed up via OAuth: {}", normalizedEmail);
        }

        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }

    @Transactional
    public String registerUser(ProfileRequest profileRequest) throws IOException {
        String email = profileRequest.getEmail().toLowerCase();
        String phone = profileRequest.getPhone().toLowerCase();

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.findByPhoneIgnoreCase(phone).isPresent()) {
            throw new RuntimeException("Phone number already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(generateRandomPassword()));
        user.setRole("USER");
        updateUserProfile(user, profileRequest);

        userRepository.save(user);
        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }

    @Transactional
    public void updateProfileAfterRegistration(String email, ProfileRequest profileRequest) throws IOException {
        updateUserProfile(email, profileRequest);
    }

    @Transactional
    public void saveOAuthUser(UserEntity user) {
        String normalizedEmail = user.getEmail() != null ? user.getEmail().toLowerCase() : null;
        logger.info("Saving OAuth user with email: {}", normalizedEmail);

        if (normalizedEmail != null && userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            logger.info("Updating existing OAuth user: {}", normalizedEmail);
            UserEntity existingUser = userRepository.findByEmailIgnoreCase(normalizedEmail).get();
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setRole(user.getRole() != null ? user.getRole() : "USER");
            userRepository.save(existingUser);
        } else {
            logger.info("Creating new OAuth user: {}", normalizedEmail);
            user.setEmail(normalizedEmail);
            user.setPassword(passwordEncoder.encode("oauth-" + user.getFirstName() + "-" + System.currentTimeMillis()));
            user.setRole(user.getRole() != null ? user.getRole() : "USER");
            userRepository.save(user);
        }
    }

    @Transactional
    public void sendPasswordResetLink(String email) throws MessagingException {
        String normalizedEmail = email.toLowerCase();
        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email));
        tokenRepository.deleteByUserId(user.getId());
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);
        String resetLink = "http://localhost:9090/auth/reset-password?token=" + token;
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(normalizedEmail);
        helper.setSubject("Password Reset Request - Job Portal");
        helper.setText(
                "<h2>Job Portal Password Reset</h2>" +
                        "<p>You requested a password reset. Click the link below to reset your password:</p>" +
                        "<a href=\"" + resetLink + "\">Reset Password</a>" +
                        "<p>This link will expire in 1 hour.</p>" +
                        "<p>If you did not request this, please ignore this email.</p>", true
        );
        mailSender.send(message);
        logger.info("Password reset link sent to {}", normalizedEmail);
    }

    @Transactional
    public void validateResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Token has expired");
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Token has expired");
        }
        UserEntity user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }

    private void updateUserProfile(String email, ProfileRequest profileRequest) throws IOException {
        if (profileRequest.getFirstName() == null || profileRequest.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (profileRequest.getSkills() == null || profileRequest.getSkills().isEmpty()) {
            throw new IllegalArgumentException("At least one skill is required");
        }
        UserEntity user = findByEmail(email);
        updateUserProfile(user, profileRequest);
        userRepository.save(user);
        logger.info("Profile updated for user: {}", email);
    }

    private void updateUserProfile(UserEntity user, ProfileRequest profileRequest) throws IOException {
        user.setFirstName(profileRequest.getFirstName());
        user.setLastName(profileRequest.getLastName());
        user.setAddress(profileRequest.getAddress());
        user.setZipCode(profileRequest.getZipCode());
        user.setExperienceLevel(profileRequest.getExperienceLevel());
        user.setAreaOfInterest(profileRequest.getAreaOfInterest());
        if (profileRequest.getResume() != null && !profileRequest.getResume().isEmpty()) {
            try {
                String resumePath = saveResumeFile(profileRequest.getResume());
                user.setResumePath(resumePath);
            } catch (IOException e) {
                logger.error("Failed to save resume for user {}: {}", user.getEmail(), e.getMessage());
                throw new IOException("Unable to save resume file: " + e.getMessage(), e);
            }
        }
        user.setSkills(profileRequest.getSkills());
        user.setPreferredRoles(profileRequest.getPreferredRoles());
        user.setPreferredLocations(profileRequest.getPreferredLocations());
        user.setCommunitiesOfInterest(profileRequest.getCommunitiesOfInterest());
        user.setReferralSource(profileRequest.getReferralSource());
        user.setGender(profileRequest.getGender());
        user.setHasDisability(profileRequest.getHasDisability());

        user.getEducationHistory().clear();
        for (EducationRequest edu : profileRequest.getEducationHistory()) {
            if (edu.getDegree() != null && !edu.getDegree().isBlank()) {
                EducationEntity education = new EducationEntity();
                education.setDegree(edu.getDegree());
                education.setInstitution(edu.getInstitution());
                education.setStartDate(edu.getStartDate());
                education.setEndDate(edu.getEndDate());
                education.setUser(user);
                user.getEducationHistory().add(education);
            }
        }

        user.getWorkExperience().clear();
        for (WorkExperienceRequest work : profileRequest.getWorkExperience()) {
            if (work.getJobTitle() != null && !work.getJobTitle().isBlank()) {
                WorkExperienceEntity experience = new WorkExperienceEntity();
                experience.setJobTitle(work.getJobTitle());
                experience.setCompany(work.getCompany());
                experience.setStartDate(work.getStartDate());
                experience.setEndDate(work.getEndDate());
                experience.setResponsibilities(work.getResponsibilities());
                experience.setUser(user);
                user.getWorkExperience().add(experience);
            }
        }
    }

    private String generateRandomPassword() {
        return String.format("%06d", random.nextInt(999999));
    }

    private String saveResumeFile(MultipartFile resume) throws IOException {
        if (resume == null || resume.isEmpty()) {
            logger.error("Resume file is null or empty");
            throw new IOException("Resume file is null or empty");
        }
        if (resume.getOriginalFilename() == null || resume.getOriginalFilename().isBlank()) {
            logger.error("Resume file has no valid filename");
            throw new IOException("Resume file has no valid filename");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath);
            }
            String fileName = System.currentTimeMillis() + "_" + resume.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, resume.getBytes());
            logger.info("Saved resume file to: {}", filePath);
            return filePath.toString();
        } catch (IOException e) {
            logger.error("Failed to save resume file: {}", e.getMessage());
            throw new IOException("Unable to save resume file: " + e.getMessage(), e);
        }
    }
}