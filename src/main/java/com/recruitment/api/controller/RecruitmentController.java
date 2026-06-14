package com.recruitment.api.controller;

import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;
import com.recruitment.api.repository.ApplicantRepository;
import com.recruitment.api.repository.JobListingRepository;
import com.recruitment.api.service.RecruitmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recruitment")
public class RecruitmentController {

    private final RecruitmentService recruitmentService;
    private final ApplicantRepository applicantRepository;
    private final JobListingRepository jobListingRepository;

    public RecruitmentController(RecruitmentService recruitmentService,
                                 ApplicantRepository applicantRepository,
                                 JobListingRepository jobListingRepository) {
        this.recruitmentService = recruitmentService;
        this.applicantRepository = applicantRepository;
        this.jobListingRepository = jobListingRepository;
    }

    /**
     * Endpoint to filter applications based on job listing ID, minimum experience, and certification requirements.
     * Route: GET /api/recruitment/applications/filter
     */
    @GetMapping("/applications/filter")
    public ResponseEntity<List<Applicant>> filterApplications(
            @RequestParam Long jobListingId,
            @RequestParam Integer minExperience,
            @RequestParam(defaultValue = "false") Boolean requiresCertification) {
        // TODO: 1. Validate the input request parameters.
        if (jobListingId == null|| jobListingId<= 0) {
            return ResponseEntity.badRequest().build();
        }
        if (minExperience == null || minExperience <= 0) {
            return ResponseEntity.badRequest().build();
        }

        //  Call recruitmentService.filterApplications(jobListingId, minExperience, requiresCertification).
        List<Applicant> filteredApplications= recruitmentService.filterApplications(jobListingId, minExperience, requiresCertification);

        // Return the list of filtered applicants wrapped in a ResponseEntity with HTTP status OK.
        return ResponseEntity.ok(filteredApplications);
    }

    /**
     * Endpoint to calculate a match score between a specific applicant and a job listing.
     * Route: GET /api/recruitment/match-score
     */
    @GetMapping("/match-score")
    public ResponseEntity<Double> getMatchScore(
            @RequestParam Long applicantId,
            @RequestParam Long jobListingId) {
        Optional<Applicant> applicantOpt= applicantRepository.findById(applicantId);
        // TODO: 2. Retrieve the JobListing entity using jobListingRepository.findById(jobListingId).
        Optional<JobListing> jobListingOpt = jobListingRepository.findById(jobListingId);

        if (applicantOpt.isEmpty()||jobListingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // TODO: 3. Call recruitmentService.calculateMatchScore(applicant, jobListing).
        Double score = recruitmentService.calculateMatchScore(applicantOpt.get(), jobListingOpt.get());
        // TODO: 4. Return the calculated match score wrapped in a ResponseEntity with HTTP status OK.
        return new ResponseEntity<>(score, HttpStatus.OK);
    }

    /**
     * Endpoint to bulk update candidate statuses.
     * Route: POST /api/recruitment/applications/bulk-status
     */
    @PostMapping("/applications/bulk-status")
    public ResponseEntity<Void> bulkUpdateStatuses(@RequestBody BulkStatusUpdateRequest request) {
        // TODO: 1. Validate that the request body is not null and has non-empty fields.
        if (request == null|| request.getApplicantIds() == null
                || request.getApplicantIds().isEmpty()
                || request.getNewStatus() == null
                || request.getNewStatus().isBlank())
        {
            return ResponseEntity.badRequest().build();
        }
        // TODO: 2. Call recruitmentService.bulkUpdateCandidateStatuses(request.getApplicantIds(), request.getNewStatus()).
        recruitmentService.bulkUpdateCandidateStatuses(request.getApplicantIds(), request.getNewStatus());
        // TODO: 3. Return a ResponseEntity indicating success (e.g., HTTP status 200 OK or 204 No Content).
        return ResponseEntity.ok().build();
    }

    /**
     * DTO class to represent the bulk status update payload.
     */
    public static class BulkStatusUpdateRequest {
        private List<Long> applicantIds;
        private String newStatus;

        public BulkStatusUpdateRequest() {}

        public BulkStatusUpdateRequest(List<Long> applicantIds, String newStatus) {
            this.applicantIds = applicantIds;
            this.newStatus = newStatus;
        }

        public List<Long> getApplicantIds() {
            return applicantIds;
        }

        public void setApplicantIds(List<Long> applicantIds) {
            this.applicantIds = applicantIds;
        }

        public String getNewStatus() {
            return newStatus;
        }

        public void setNewStatus(String newStatus) {
            this.newStatus = newStatus;
        }
    }
}
