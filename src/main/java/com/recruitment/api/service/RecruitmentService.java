package com.recruitment.api.service;

import com.recruitment.api.dto.ApplicantDto;
import com.recruitment.api.dto.CreateApplicantRequest;
import com.recruitment.api.dto.JobListingDto;
import com.recruitment.api.dto.RecruitmentMapper;
import com.recruitment.api.exception.ResourceNotFoundException;
import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;
import com.recruitment.api.repository.ApplicantRepository;
import com.recruitment.api.repository.JobListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecruitmentService {

    private static final double EXPERIENCE_WEIGHT = 70.0;
    private static final double CERTIFICATION_WEIGHT = 30.0;

    private final ApplicantRepository applicantRepository;
    private final JobListingRepository jobListingRepository;

    public RecruitmentService(ApplicantRepository applicantRepository, JobListingRepository jobListingRepository) {
        this.applicantRepository = applicantRepository;
        this.jobListingRepository = jobListingRepository;
    }

    /**
     * Filters applicants based on job listing requirements.
     */
    public List<ApplicantDto> filterApplications(Long jobListingId, Integer minExperience, Boolean requiresCertification) {
        if (jobListingId == null) {
            throw new IllegalArgumentException("jobListingId must not be null");
        }
        if (minExperience == null || minExperience < 0) {
            throw new IllegalArgumentException("minExperience must be a non-negative number");
        }

        if (!jobListingRepository.existsById(jobListingId)) {
            throw new ResourceNotFoundException("No job listing found with id " + jobListingId);
        }

        boolean certRequired = Boolean.TRUE.equals(requiresCertification);

        return applicantRepository.findByJobListingId(jobListingId).stream()
                .filter(applicant -> applicant.getYearsOfExperience() != null
                        && applicant.getYearsOfExperience() >= minExperience)
                .filter(applicant -> !certRequired || Boolean.TRUE.equals(applicant.getHasCertification()))
                .map(RecruitmentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Calculates a match score (0-100) between an applicant and a job listing,
     * weighted by years of experience relative to the minimum requirement and
     * whether the applicant holds a required certification.
     */
    public double calculateMatchScore(Long applicantId, Long jobListingId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("No applicant found with id " + applicantId));
        JobListing jobListing = jobListingRepository.findById(jobListingId)
                .orElseThrow(() -> new ResourceNotFoundException("No job listing found with id " + jobListingId));

        double experienceScore = calculateExperienceScore(applicant, jobListing);
        double certificationScore = calculateCertificationScore(applicant, jobListing);

        return Math.round((experienceScore + certificationScore) * 100.0) / 100.0;
    }

    // Full credit once years of experience meets the minimum; prorated (never negative,
    // never over 100%) if the applicant falls short. A listing with no stated minimum
    // awards full credit automatically since there's nothing to fall short of.
    private double calculateExperienceScore(Applicant applicant, JobListing jobListing) {
        int required = jobListing.getMinimumWorkExperience() == null ? 0 : jobListing.getMinimumWorkExperience();
        int actual = applicant.getYearsOfExperience() == null ? 0 : applicant.getYearsOfExperience();

        if (required <= 0) {
            return EXPERIENCE_WEIGHT;
        }
        double ratio = Math.min(1.0, (double) actual / required);
        return ratio * EXPERIENCE_WEIGHT;
    }

    // All-or-nothing: full credit if certification isn't required, or if it's required
    // and the applicant has it; zero if it's required and they don't.
    private double calculateCertificationScore(Applicant applicant, JobListing jobListing) {
        boolean certRequired = Boolean.TRUE.equals(jobListing.getRequiredCertification());
        if (!certRequired) {
            return CERTIFICATION_WEIGHT;
        }
        return Boolean.TRUE.equals(applicant.getHasCertification()) ? CERTIFICATION_WEIGHT : 0.0;
    }

    /**
     * Lists all open job listings.
     */
    public List<JobListingDto> listJobListings() {
        return jobListingRepository.findAll().stream()
                .map(RecruitmentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new applicant against a job listing. New applicants start out PENDING.
     */
    public ApplicantDto createApplicant(CreateApplicantRequest request) {
        JobListing jobListing = jobListingRepository.findById(request.getJobListingId())
                .orElseThrow(() -> new ResourceNotFoundException("No job listing found with id " + request.getJobListingId()));

        Applicant applicant = RecruitmentMapper.fromCreateRequest(request, jobListing);
        return RecruitmentMapper.toDto(applicantRepository.save(applicant));
    }

    /**
     * Looks up a single applicant, e.g. for a status check.
     */
    public ApplicantDto getApplicant(Long applicantId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("No applicant found with id " + applicantId));
        return RecruitmentMapper.toDto(applicant);
    }

    /**
     * Bulk updates the statuses of multiple candidate applications.
     */
    @Transactional
    public void bulkUpdateCandidateStatuses(List<Long> applicantIds, String newStatus) {
        if (applicantIds == null || applicantIds.isEmpty()) {
            throw new IllegalArgumentException("applicantIds must not be empty");
        }
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("newStatus must not be blank");
        }

        List<Applicant> applicants = applicantRepository.findAllById(applicantIds);
        applicants.forEach(applicant -> applicant.setStatus(newStatus));
        applicantRepository.saveAll(applicants);
    }
}
