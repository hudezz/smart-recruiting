package com.recruitment.api.controller;

import com.recruitment.api.dto.ApplicantDto;
import com.recruitment.api.dto.CreateApplicantRequest;
import com.recruitment.api.dto.JobListingDto;
import com.recruitment.api.service.RecruitmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST surface for both sides of the hiring flow: the public application
 * endpoints ({@code /job-listings}, {@code /applicants}) used by candidates,
 * and the recruiter endpoints ({@code /applications/*}, {@code /match-score})
 * used by the admin docket. Request validation happens here; everything else
 * (not-found handling, business rules) lives in {@link RecruitmentService}.
 */
@RestController
@RequestMapping("/api/recruitment")
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    public RecruitmentController(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }

    /**
     * Endpoint to list all open job listings.
     * Route: GET /api/recruitment/job-listings
     */
    @GetMapping("/job-listings")
    public ResponseEntity<List<JobListingDto>> listJobListings() {
        return ResponseEntity.ok(recruitmentService.listJobListings());
    }

    /**
     * Endpoint for a candidate to submit a new application.
     * Route: POST /api/recruitment/applicants
     */
    @PostMapping("/applicants")
    public ResponseEntity<ApplicantDto> createApplicant(@Valid @RequestBody CreateApplicantRequest request) {
        ApplicantDto created = recruitmentService.createApplicant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Endpoint for a candidate to check their application status.
     * Route: GET /api/recruitment/applicants/{applicantId}
     */
    @GetMapping("/applicants/{applicantId}")
    public ResponseEntity<ApplicantDto> getApplicant(@PathVariable Long applicantId) {
        return ResponseEntity.ok(recruitmentService.getApplicant(applicantId));
    }

    /**
     * Endpoint to filter applications based on job listing ID, minimum experience, and certification requirements.
     * Route: GET /api/recruitment/applications/filter
     */
    @GetMapping("/applications/filter")
    public ResponseEntity<List<ApplicantDto>> filterApplications(
            @RequestParam Long jobListingId,
            @RequestParam Integer minExperience,
            @RequestParam(defaultValue = "false") Boolean requiresCertification) {

        // Validate basic HTTP request inputs
        if (jobListingId == null || jobListingId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        if (minExperience == null || minExperience < 0) {
            return ResponseEntity.badRequest().build();
        }

        List<ApplicantDto> dtos = recruitmentService.filterApplications(jobListingId, minExperience, requiresCertification);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Endpoint to calculate a match score between a specific applicant and a job listing.
     * Route: GET /api/recruitment/match-score
     */
    @GetMapping("/match-score")
    public ResponseEntity<Double> getMatchScore(
            @RequestParam Long applicantId,
            @RequestParam Long jobListingId) {

        if (applicantId == null || applicantId <= 0 || jobListingId == null || jobListingId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        double score = recruitmentService.calculateMatchScore(applicantId, jobListingId);
        return ResponseEntity.ok(score);
    }

    /**
     * Endpoint to bulk update candidate statuses.
     * Route: POST /api/recruitment/applications/bulk-status
     */
    @PostMapping("/applications/bulk-status")
    public ResponseEntity<Void> bulkUpdateStatuses(@RequestBody BulkStatusUpdateRequest request) {

        if (request == null || request.getApplicantIds() == null
                || request.getApplicantIds().isEmpty()
                || request.getNewStatus() == null
                || request.getNewStatus().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        recruitmentService.bulkUpdateCandidateStatuses(request.getApplicantIds(), request.getNewStatus());

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