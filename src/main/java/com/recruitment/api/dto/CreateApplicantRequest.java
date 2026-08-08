package com.recruitment.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public-facing request body for a candidate submitting a new application
 * (POST /applicants). Deliberately narrower than the {@code Applicant} entity:
 * it excludes internal/sensitive fields like {@code password} and
 * {@code nationalId} that a public form has no business collecting or setting.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicantRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    private String phoneNumber;

    @NotNull
    @Min(0)
    private Integer yearsOfExperience;

    @NotNull
    private Boolean hasCertification;

    // Which listing this application is for; validated to exist in RecruitmentService#createApplicant.
    @NotNull
    private Long jobListingId;
}
