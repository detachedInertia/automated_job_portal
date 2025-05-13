package com.project.automated_job_portal.security;

import com.project.automated_job_portal.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final List<RequestMatcher> excludedMatchers = Arrays.asList(
            new AntPathRequestMatcher("/"),
            new AntPathRequestMatcher("/css/**"),
            new AntPathRequestMatcher("/js/**"),
            new AntPathRequestMatcher("/images/**"),
            new AntPathRequestMatcher("/favicon.ico"),
            new AntPathRequestMatcher("/login"),
            new AntPathRequestMatcher("/login-email"),
            new AntPathRequestMatcher("/auth/otp-request"),
            new AntPathRequestMatcher("/auth/request-otp"),
            new AntPathRequestMatcher("/auth/verify-otp"),
            new AntPathRequestMatcher("/auth/otp-login"),
            new AntPathRequestMatcher("/auth/form-login"),
            new AntPathRequestMatcher("/auth/login-success"),
            new AntPathRequestMatcher("/auth/register"),
            new AntPathRequestMatcher("/auth/signup"),
            new AntPathRequestMatcher("/auth/forgot-password"),
            new AntPathRequestMatcher("/auth/reset-password"),
            new AntPathRequestMatcher("/oauth2/**"),
            new AntPathRequestMatcher("/login/oauth2/code/**"),
            new AntPathRequestMatcher("/contact-us"),
            new AntPathRequestMatcher("/help"),
            new AntPathRequestMatcher("/app-settings"),
            new AntPathRequestMatcher("/jobs")
    );

    @Autowired
    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Skip filter for excluded paths
        for (RequestMatcher matcher : excludedMatchers) {
            if (matcher.matches(request)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Try to get the token from the Authorization header (for API requests)
        String token = null;
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            logger.debug("Token found in Authorization header: {}", token);
        }

        // If token not found in header, try to get it from the cookie (for Thymeleaf requests)
        if (token == null && request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwtToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
            logger.debug("Token found in cookie: {}", token);
        }

        // Validate the token and set authentication
        if (token != null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    List<String> roles = jwtUtil.getRolesFromToken(token);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = User.withUsername(username)
                                .password("")
                                .roles(roles.toArray(new String[0]))
                                .build();

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("Authentication set for user: {} with roles: {}", username, roles);
                    } else {
                        logger.warn("Username is null or authentication already exists for token: {}", token);
                    }
                } else {
                    logger.warn("Invalid JWT token: {}", token);
                }
            } catch (Exception e) {
                logger.error("Error validating JWT token: {}", e.getMessage());
            }
        } else {
            logger.debug("No JWT token found in request");
        }

        filterChain.doFilter(request, response);
    }
}