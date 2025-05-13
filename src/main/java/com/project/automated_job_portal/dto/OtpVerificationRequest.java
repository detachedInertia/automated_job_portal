package com.project.automated_job_portal.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpVerificationRequest {

    @NotBlank(message = "Identifier is required")
    private String identifier;

    @NotBlank(message = "OTP is required")
    private String otp;

    public OtpVerificationRequest() {}

    public OtpVerificationRequest(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}