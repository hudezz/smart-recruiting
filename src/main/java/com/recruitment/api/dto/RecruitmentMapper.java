package com.recruitment.api.dto;

import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;

public class RecruitmentMapper {

    /**
     * Builds a new {@link Applicant} entity from a public application submission.
     * New applicants always start life as PENDING — status is only ever changed
     * afterward via the bulk-status-update endpoint. resumeText is left unset since
     * the public application form doesn't collect it yet.
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

    public static JobListingDto toDto(JobListing entity) {
        if (entity == null) {
            return null;
        }
        return JobListingDto.builder()
                .id(entity.getId())
                .jobName(entity.getJobName())
                .jobDescription(entity.getJobDescription())
                .minimumWorkExperience(entity.getMinimumWorkExperience())
                .requiredCertification(entity.getRequiredCertification())
                .build();
    }

    public static JobListing toEntity(JobListingDto dto) {
        if (dto == null) {
            return null;
        }
        JobListing entity = new JobListing();
        entity.setId(dto.getId());
        entity.setJobName(dto.getJobName());
        entity.setJobDescription(dto.getJobDescription());
        entity.setMinimumWorkExperience(dto.getMinimumWorkExperience());
        entity.setRequiredCertification(dto.getRequiredCertification());
        return entity;
    }

    public static ApplicantDto toDto(Applicant entity) {
        if (entity == null) {
            return null;
        }
        return ApplicantDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .nationalId(entity.getNationalId())
                .yearsOfExperience(entity.getYearsOfExperience())
                .hasCertification(entity.getHasCertification())
                .status(entity.getStatus())
                .jobListing(toDto(entity.getJobListing()))
                .resumeText(entity.getResumeText())
                .build();
    }

    public static Applicant toEntity(ApplicantDto dto) {
        if (dto == null) {
            return null;
        }
        Applicant entity = new Applicant();
        entity.setId(dto.getId());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setNationalId(dto.getNationalId());
        entity.setYearsOfExperience(dto.getYearsOfExperience());
        entity.setHasCertification(dto.getHasCertification());
        entity.setStatus(dto.getStatus());
        entity.setJobListing(toEntity(dto.getJobListing()));
        entity.setResumeText(dto.getResumeText());
        return entity;
    }
}
