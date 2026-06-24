package com.recruitment.api.controller;

import com.recruitment.api.dto.ApplicantDto;
import com.recruitment.api.service.RecruitmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruitment")
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    public RecruitmentController(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
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

        // TODO: Call service layer to get filtered ApplicantDto list
        // List<ApplicantDto> dtos = recruitmentService.filterApplications(...);

        return ResponseEntity.ok(null); // TODO: Return the result
    }

    /**
     * Endpoint to calculate a match score between a specific applicant and a job listing.
     * Route: GET /api/recruitment/match-score
     */
    @GetMapping("/match-score")
    public ResponseEntity<Double> getMatchScore(
            @RequestParam Long applicantId,
            @RequestParam Long jobListingId) {

        // TODO: Call service layer to fetch entities, calculate score, and return score value
        // Double score = recruitmentService.calculateMatchScore(...);

        return ResponseEntity.ok(null); // TODO: Return the calculated score
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

        // TODO: Call service layer to update candidate statuses
        // recruitmentService.bulkUpdateCandidateStatuses(...);

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