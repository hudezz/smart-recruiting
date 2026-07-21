package com.recruitment.api.dto;

import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;

public class RecruitmentMapper {

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

