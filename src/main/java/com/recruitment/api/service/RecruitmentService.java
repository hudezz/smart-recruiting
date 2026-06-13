package com.recruitment.api.service;

import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;
import com.recruitment.api.repository.ApplicantRepository;
import com.recruitment.api.repository.JobListingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecruitmentService {

    private final ApplicantRepository applicantRepository;
    private final JobListingRepository jobListingRepository;

    public RecruitmentService(ApplicantRepository applicantRepository, JobListingRepository jobListingRepository) {
        this.applicantRepository = applicantRepository;
        this.jobListingRepository = jobListingRepository;
    }

    /**
     * Filters applicants based on job listing requirements.
     */
    public List<Applicant> filterApplications(Long jobListingId, Integer minExperience, Boolean requiresCertification) {
        // Validate input parameters
        if (jobListingId == null) {
            throw new IllegalArgumentException("jobListingId cannot be null");
        }

        // Retrieve the JobListing entity from jobListingRepository.
        JobListing jobListing = jobListingRepository.findById(jobListingId)
                .orElseThrow(() -> new EntityNotFoundException("jobListingId not found"));

        // Querying applicants associated with the jobListingId
        List<Applicant> matchedApplicants = applicantRepository.findByJobListingId(jobListingId);

        // Post-query filtering (handling null checks for safety)
        return matchedApplicants.stream()
                .filter(app -> app.getYearsOfExperience() != null && app.getYearsOfExperience() >= minExperience)
                .filter(app -> !Boolean.TRUE.equals(requiresCertification) || Boolean.TRUE.equals(app.getHasCertification()))
                .toList();
    }

    /**
     * Calculates a match score between an applicant and a job listing.
     */
    public double calculateMatchScore(Applicant applicant, JobListing jobListing) {
        double score = 0.0;

        // 1. Experience Check
        if (applicant.getYearsOfExperience() != null && jobListing.getMinimumWorkExperience() != null) {
            if (applicant.getYearsOfExperience() >= jobListing.getMinimumWorkExperience()) {
                score += 50.0;
            }
        }

        // 2. Certification Check
        // Handles case where getRequiredCertification() might return null
        if (Boolean.TRUE.equals(jobListing.getRequiredCertification())) {
            if (Boolean.TRUE.equals(applicant.getHasCertification())) {
                score += 50.0;
            }
        } else {
            // If the job doesn't require certification, grant the 50 points automatically
            score += 50.0;
        }

        // 3. Normalize score (0.0 to 100.0)
        double maxPossibleScore = 100.0;
        return (score / maxPossibleScore) * 100.0;
    }

    /**
     * Bulk updates the statuses of multiple candidate applications.
     */
    public void bulkUpdateCandidateStatuses(List<Long> applicantIds, String newStatus) {
        // FIXED: Added the logical OR (||) operator
        if (applicantIds == null || applicantIds.isEmpty()) {
            throw new IllegalArgumentException("applicantIds cannot be null or empty");
        }

        // FIXED: Added the logical OR (||) operator
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("newStatus cannot be null or empty");
        }

        // Retrieve the list of Applicant entities
        List<Applicant> applicants = applicantRepository.findAllById(applicantIds);
        if (applicants.isEmpty()) {
            throw new EntityNotFoundException("No applicants found for the provided IDs");
        }

        // Iterate and update status
        for (Applicant applicant : applicants) {
            applicant.setStatus(newStatus);
        }

        // Save the updated list back to the database
        applicantRepository.saveAll(applicants);

        // Optional: Log action
        System.out.println("Successfully bulk updated " + applicants.size() + " applicants to status: " + newStatus);
    }
}