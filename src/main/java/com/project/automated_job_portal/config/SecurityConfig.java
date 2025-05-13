package com.project.automated_job_portal.config;

import com.project.automated_job_portal.security.JwtFilter;
import com.project.automated_job_portal.security.OAuth2SuccessHandler;
import com.project.automated_job_portal.service.UserService;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final UserService userService;

    @Autowired
    public SecurityConfig(JwtFilter jwtFilter, OAuth2SuccessHandler oAuth2SuccessHandler, UserService userService) {
        this.jwtFilter = jwtFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // Update the authorizeHttpRequests section in SecurityConfig.java
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/login", "/login-email", "/auth/otp-request", "/auth/request-otp",
                                "/auth/verify-otp", "/auth/otp-login", "/auth/form-login", "/auth/login-success",
                                "/auth/register", "/signup", "/auth/forgot-password", "/auth/reset-password",
                                "/auth/complete-profile").permitAll()
                        .requestMatchers("/contact-us", "/help", "/app-settings", "/jobs", "/auth/contact-us", "/auth/help").permitAll() // Allow public access
                        .requestMatchers("/oauth2/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers("/auth/post-job").hasRole("EMPLOYER")
                        .requestMatchers("/auth/dashboard", "/auth/invites", "/auth/settings", "/auth/jobs-applied", "/auth/watchlist").hasAnyRole("USER", "EMPLOYER", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/employer/**").hasRole("EMPLOYER")
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated()

                )
                .formLogin(form -> form
                        .loginPage("/login-email")
                        .loginProcessingUrl("/auth/form-login")
                        .successHandler((request, response, authentication) -> {
                            String email = authentication.getName();
                            String token = userService.loginWithForm(email);
                            Cookie jwtCookie = new Cookie("jwtToken", token);
                            jwtCookie.setHttpOnly(true);
                            jwtCookie.setPath("/");
                            jwtCookie.setMaxAge(3600);
                            response.addCookie(jwtCookie);
                            response.sendRedirect("/auth/login-success");
                        })
                        .failureUrl("/login-email?error=Invalid%20credentials")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login-email")
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/oauth2/authorization")
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl("/login-email?error=OAuth2%20authentication%20failed")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login-email?logout=true")
                        .deleteCookies("jwtToken")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userService);
        return authBuilder.build();
    }
}