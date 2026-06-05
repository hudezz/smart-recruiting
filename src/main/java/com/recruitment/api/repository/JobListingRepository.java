package com.recruitment.api.repository;

import com.recruitment.api.model.JobListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobListingRepository extends JpaRepository<JobListing, Long> {
    List<JobListing> findByJobNameAndJobDescriptionAndRequiredCertification(String jobName, String jobDescription, Boolean requiredCertification);

}
