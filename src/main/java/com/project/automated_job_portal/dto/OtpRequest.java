package com.project.automated_job_portal.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpRequest {

    @NotBlank(message = "Email or phone is required")
    private String identifier;

    public OtpRequest() {}

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
}