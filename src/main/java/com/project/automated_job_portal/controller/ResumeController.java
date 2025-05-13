package com.project.automated_job_portal.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @PostMapping("/generate")
    public ResponseEntity<ByteArrayResource> generateResume(@RequestParam Long jobId, @RequestParam Long userId) {
        RestTemplate restTemplate = new RestTemplate();
        String resumeUrl = "http://localhost:8000/generate-resume";

        // Make the API call to the external service
        String response = restTemplate.postForObject(resumeUrl, null, String.class);

        // Placeholder: Convert the response to a byte array (e.g., PDF content)
        byte[] pdfBytes = response.getBytes(); // Replace with actual logic to parse response into PDF bytes

        // Create ByteArrayResource from the byte array
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=resume.pdf")
                .contentLength(pdfBytes.length)
                .body(resource);
    }
}