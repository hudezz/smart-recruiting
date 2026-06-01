package com.recruitment.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Applicant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private String nationalId;
    private Integer yearsOfExperience;
    private Boolean hasCertification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_listing_id")
    private JobListing jobListing;

}
