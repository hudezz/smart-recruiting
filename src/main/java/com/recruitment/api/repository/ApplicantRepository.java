package com.recruitment.api.repository;

import com.recruitment.api.model.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
    List<Applicant> findByJobListingIdAndYearsOfExperienceGreaterThanEqual(Long jobListingId, Integer years);


}
