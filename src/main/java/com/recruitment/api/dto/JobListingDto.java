package com.recruitment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobListingDto {
    private Long id;
    private String jobName;
    private String jobDescription;
    private Integer minimumWorkExperience;
    private Boolean requiredCertification;
}
