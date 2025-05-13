package com.project.automated_job_portal.config;

import com.project.automated_job_portal.entity.*;
import com.project.automated_job_portal.repository.JobRepository;
import com.project.automated_job_portal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(JobRepository jobRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create a default admin user if none exists
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            UserEntity admin = new UserEntity();
            admin.setEmail("admin@example.com");
            admin.setPhone("1234567890");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole("ADMIN");
            // Adjust fields to match UserEntity's existing fields
            admin.setExperienceLevel("10 years");
            // admin.setCurrentCompany("Career Connect Inc."); // No such field in UserEntity, removing
            admin.setAddress("Remote");
            // admin.setResumeHeadline("Experienced Software Engineer and Administrator"); // No such field, removing
            admin.setSkills(Arrays.asList("Java", "Spring Boot", "React", "AWS", "Docker"));

            // Education
            EducationEntity education1 = new EducationEntity();
            education1.setUser(admin);
            education1.setDegree("B.Tech in Computer Science");
            education1.setInstitution("IIT Delhi");
            // education1.setCompletionYear("2007"); // No completionYear field, using endDate instead
            education1.setEndDate("2007");

            EducationEntity education2 = new EducationEntity();
            education2.setUser(admin);
            education2.setDegree("M.Tech in Software Engineering");
            education2.setInstitution("IIT Bombay");
            education2.setEndDate("2009");

            admin.setEducationHistory(Arrays.asList(education1, education2));

            // Work Experience
            WorkExperienceEntity workExp1 = new WorkExperienceEntity();
            workExp1.setUser(admin);
            // workExp1.setCompanyName("TechCorp"); // Use 'company' instead
            workExp1.setCompany("TechCorp");
            // workExp1.setRole("Software Engineer"); // Use 'jobTitle' instead
            workExp1.setJobTitle("Software Engineer");
            workExp1.setStartDate("Jan 2010");
            workExp1.setEndDate("Dec 2015");
            // workExp1.setDescription("Developed web applications using Java and Spring."); // Use 'responsibilities'
            workExp1.setResponsibilities("Developed web applications using Java and Spring.");

            WorkExperienceEntity workExp2 = new WorkExperienceEntity();
            workExp2.setUser(admin);
            workExp2.setCompany("Career Connect Inc.");
            workExp2.setJobTitle("Senior Administrator");
            workExp2.setStartDate("Jan 2016");
            workExp2.setEndDate("Present");
            workExp2.setResponsibilities("Managed job portal platform and led development teams.");

            admin.setWorkExperience(Arrays.asList(workExp1, workExp2));

            userRepository.save(admin);
        }

        // Seed the database with jobs and internships if empty
        if (jobRepository.count() == 0) {
            UserEntity admin = userRepository.findByEmail("admin@example.com").orElseThrow();

            // Jobs
            JobEntity job1 = new JobEntity();
            job1.setTitle("Software Engineer - Job Portal");
            job1.setDescription("Develop and maintain web applications for our Job Portal platform.");
            job1.setCompany("JobPortal Tech Corp");
            job1.setLocation("Remote");
            job1.setExperienceLevel("Mid-Level");
            job1.setSalary(75000.0);
            job1.setPostedBy(admin);
            job1.setType("JOB");
            jobRepository.save(job1);

            JobEntity job2 = new JobEntity();
            job2.setTitle("Data Scientist - Job Portal Analytics");
            job2.setDescription("Analyze data and build machine learning models to enhance Job Portal user experience.");
            job2.setCompany("JobPortal Data Inc");
            job2.setLocation("New York");
            job2.setExperienceLevel("Senior");
            job2.setSalary(120000.0);
            job2.setPostedBy(admin);
            job2.setType("JOB");
            jobRepository.save(job2);

            // Internships
            JobEntity internship1 = new JobEntity();
            internship1.setTitle("Software Engineering Intern");
            internship1.setDescription("Assist in developing web applications.");
            internship1.setCompany("TechCorp");
            internship1.setLocation("Remote");
            internship1.setExperienceLevel("Entry-Level");
            internship1.setSalary(30000.0);
            internship1.setPostedBy(admin);
            internship1.setType("INTERNSHIP");
            jobRepository.save(internship1);

            JobEntity internship2 = new JobEntity();
            internship2.setTitle("Data Science Intern");
            internship2.setDescription("Support data analysis projects.");
            internship2.setCompany("DataInc");
            internship2.setLocation("New York");
            internship2.setExperienceLevel("Entry-Level");
            internship2.setSalary(32000.0);
            internship2.setPostedBy(admin);
            internship2.setType("INTERNSHIP");
            jobRepository.save(internship2);

            JobEntity internship3 = new JobEntity();
            internship3.setTitle("Product Management Intern");
            internship3.setDescription("Help manage product roadmaps.");
            internship3.setCompany("InnovateCo");
            internship3.setLocation("San Francisco");
            internship3.setExperienceLevel("Entry-Level");
            internship3.setSalary(31000.0);
            internship3.setPostedBy(admin);
            internship3.setType("INTERNSHIP");
            jobRepository.save(internship3);

            JobEntity internship4 = new JobEntity();
            internship4.setTitle("UX Design Intern");
            internship4.setDescription("Design user interfaces.");
            internship4.setCompany("DesignWorks");
            internship4.setLocation("Austin");
            internship4.setExperienceLevel("Entry-Level");
            internship4.setSalary(30000.0);
            internship4.setPostedBy(admin);
            internship4.setType("INTERNSHIP");
            jobRepository.save(internship4);

            JobEntity internship5 = new JobEntity();
            internship5.setTitle("Marketing Intern");
            internship5.setDescription("Assist in campaign development.");
            internship5.setCompany("MarketPro");
            internship5.setLocation("Chicago");
            internship5.setExperienceLevel("Entry-Level");
            internship5.setSalary(29000.0);
            internship5.setPostedBy(admin);
            internship5.setType("INTERNSHIP");
            jobRepository.save(internship5);
        }
    }
}