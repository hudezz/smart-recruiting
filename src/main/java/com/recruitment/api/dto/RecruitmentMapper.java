package com.recruitment.api.dto;

import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;

public class RecruitmentMapper {

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
