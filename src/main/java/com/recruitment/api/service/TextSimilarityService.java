package com.recruitment.api.service;

import com.recruitment.api.client.PythonMlClient;
import com.recruitment.api.exception.ResourceNotFoundException;
import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;
import com.recruitment.api.repository.ApplicantRepository;
import com.recruitment.api.repository.JobListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scores an applicant's resume text against a job description using the Python
 * matching microservice.
 *
 * This is deliberately separate from {@link RecruitmentService#calculateMatchScore},
 * which stays purely rule-based (70 points of experience + 30 of certification
 * = 100). Folding text similarity into that total would silently change what
 * every existing score means, so the two are reported side by side and the
 * weighting is left as a decision for whoever owns the scoring policy.
 */
@Service
public class TextSimilarityService {

    private final ApplicantRepository applicantRepository;
    private final JobListingRepository jobListingRepository;
    private final PythonMlClient pythonMlClient;

    public TextSimilarityService(ApplicantRepository applicantRepository,
                                 JobListingRepository jobListingRepository,
                                 PythonMlClient pythonMlClient) {
        this.applicantRepository = applicantRepository;
        this.jobListingRepository = jobListingRepository;
        this.pythonMlClient = pythonMlClient;
    }

    /**
     * Returns the resume-to-job-description similarity as a percentage from 0
     * to 100.
     *
     * @throws ResourceNotFoundException if either id is unknown
     * @throws IllegalArgumentException  if the applicant has no resume text or
     *                                   the listing has no description
     */
    @Transactional(readOnly = true)
    public double calculateTextSimilarity(Long applicantId, Long jobListingId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("No applicant found with id " + applicantId));
        JobListing jobListing = jobListingRepository.findById(jobListingId)
                .orElseThrow(() -> new ResourceNotFoundException("No job listing found with id " + jobListingId));

        String resumeText = applicant.getResumeText();
        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalArgumentException(
                    "Applicant " + applicantId + " has no resume text to compare");
        }

        String jobDescription = jobListing.getJobDescription();
        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException(
                    "Job listing " + jobListingId + " has no description to compare against");
        }

        return pythonMlClient.fetchSimilarityScore(jobDescription, resumeText);
    }
}
