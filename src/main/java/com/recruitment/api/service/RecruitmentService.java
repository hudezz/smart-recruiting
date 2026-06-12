package com.recruitment.api.service;

import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;
import com.recruitment.api.repository.ApplicantRepository;
import com.recruitment.api.repository.JobListingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
        //  Validate input parameters
         if (jobListingId==null)
             throw new IllegalArgumentException("jobListingId cannot be null");

        //  Retrieve the JobListing entity from jobListingRepository.
       JobListing jobListings= jobListingRepository.findById(jobListingId).orElseThrow(() ->new EntityNotFoundException("jobListingId not found"));

        // Querying applicants associated with the jobListingId who meet the minExperience and certification criteria.
       List<Applicant>matchedApplicants = applicantRepository.findByJobListingId(jobListingId);

         //post-query filtering or sorting
        List<Applicant> filteredApplicants = matchedApplicants.stream()
                .filter(app -> app.getYearsOfExperience() >= minExperience)
                .filter(app -> !requiresCertification || app.getHasCertification())
                .toList();
        // TODO: 5. Return the filtered list of applicants.
        return filteredApplicants;
    }

    /**
     * Calculates a match score between an applicant and a job listing.
     */
    public double calculateMatchScore(Applicant applicant, JobListing jobListing) {
        // TODO: 1. Initialize match score variable to 0.0.
        // TODO: 2. Check if the applicant meets the minimum work experience required by the job listing.
        //          Add weighted points to the score if they meet or exceed the requirement.
        // TODO: 3. Check if the job listing requires a certification. If so, verify if the applicant has it.
        //          Add points if they have it; deduct or fail matching if they do not.
        // TODO: 4. Calculate final normalized score (e.g., on a scale of 0.0 to 100.0) based on weighted parameters.
        // TODO: 5. Return the calculated match score.
        return 0.0;
    }

    /**
     * Bulk updates the statuses of multiple candidate applications.
     */
    public void bulkUpdateCandidateStatuses(List<Long> applicantIds, String newStatus) {
        // TODO: 1. Validate that the applicantIds list is not null/empty and the newStatus is a valid status.
        // TODO: 2. Retrieve the list of Applicant entities using applicantRepository.findAllById(applicantIds).
        // TODO: 3. Iterate through the retrieved applicants and update their status field (if defined on Applicant model).
        // TODO: 4. Save the updated list of applicants back to the database using applicantRepository.saveAll().
        // TODO: 5. (Optional) Log the bulk update action or trigger email notifications to the candidates.
    }
}
