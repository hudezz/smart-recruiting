package com.recruitment.api.dto;

import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;

public class RecruitmentMapper {

    /**
     * Builds a new {@link Applicant} entity from a public application submission.
     * New applicants always start life as PENDING — status is only ever changed
     * afterward via the bulk-status-update endpoint.
     */
    public static Applicant fromCreateRequest(CreateApplicantRequest request, JobListing jobListing) {
        Applicant applicant = new Applicant();
        applicant.setFirstName(request.getFirstName());
        applicant.setLastName(request.getLastName());
        applicant.setEmail(request.getEmail());
        applicant.setPhoneNumber(request.getPhoneNumber());
        applicant.setYearsOfExperience(request.getYearsOfExperience());
        applicant.setHasCertification(request.getHasCertification());
        applicant.setJobListing(jobListing);
        applicant.setStatus("PENDING");
        return applicant;
    }

    public static JobListingDto toDto(JobListing jobListing) {
        if (jobListing == null) {
            return null;
        }
        return JobListingDto.builder()
                .id(jobListing.getId())
                .jobName(jobListing.getJobName())
                .jobDescription(jobListing.getJobDescription())
                .minimumWorkExperience(jobListing.getMinimumWorkExperience())
                .requiredCertification(jobListing.getRequiredCertification())
                .build();
    }

    public static ApplicantDto toDto(Applicant applicant) {
        if (applicant == null) {
            return null;
        }
        return ApplicantDto.builder()
                .id(applicant.getId())
                .firstName(applicant.getFirstName())
                .lastName(applicant.getLastName())
                .email(applicant.getEmail())
                .phoneNumber(applicant.getPhoneNumber())
                .nationalId(applicant.getNationalId())
                .yearsOfExperience(applicant.getYearsOfExperience())
                .hasCertification(applicant.getHasCertification())
                .status(applicant.getStatus())
                .jobListing(toDto(applicant.getJobListing()))
                .build();
    }
}
