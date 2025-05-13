package com.project.automated_job_portal.security;

import com.project.automated_job_portal.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("securityOAuth2SuccessHandler")
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    @Autowired
    private UserService userService;

    public OAuth2SuccessHandler() {
        setDefaultTargetUrl("/auth/dashboard");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("login") != null ? oAuth2User.getAttribute("login") : oAuth2User.getAttribute("name");

        if (email == null) {
            email = username + "@oauth.placeholder";
            logger.warn("Email not found in OAuth2 attributes, using fallback: {}", email);
        }

        try {
            boolean isNewUser = !userService.existsByEmail(email);
            String token = userService.loginWithOAuth(email, username);

            if (token == null) {
                logger.error("Failed to generate JWT token for email: {}", email);
                throw new ServletException("Unable to generate JWT token");
            }

            logger.debug("Generated JWT token for email: {}", email);
            Cookie jwtCookie = new Cookie("jwtToken", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600);
            response.addCookie(jwtCookie);
            setDefaultTargetUrl("/auth/dashboard");


            super.onAuthenticationSuccess(request, response, authentication);
        } catch (Exception e) {
            logger.error("Error during OAuth2 login: {}", e.getMessage());
            throw new ServletException("OAuth2 login failed", e);
        }
    }
}