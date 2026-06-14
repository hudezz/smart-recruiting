package com.recruitment.api.controller;

import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;
import com.recruitment.api.repository.ApplicantRepository;
import com.recruitment.api.repository.JobListingRepository;
import com.recruitment.api.service.RecruitmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecruitmentControllerTest {

    @Mock
    private RecruitmentService recruitmentService;

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private JobListingRepository jobListingRepository;

    @InjectMocks
    private RecruitmentController recruitmentController;

    private Applicant applicant;
    private JobListing jobListing;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jobListing = new JobListing(1L, "Software Engineer", "Description", 3, true);
        applicant = new Applicant(1L, "Alice", "Smith", "alice@example.com", "pass", "123", "NID1", 4, true, jobListing, "PENDING");
    }

    @Test
    void testFilterApplications_Success() {
        when(recruitmentService.filterApplications(1L, 3, true))
                .thenReturn(Collections.singletonList(applicant));

        ResponseEntity<List<Applicant>> response = recruitmentController.filterApplications(1L, 3, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Alice", response.getBody().get(0).getFirstName());
    }

    @Test
    void testFilterApplications_BadRequest_InvalidId() {
        ResponseEntity<List<Applicant>> response = recruitmentController.filterApplications(-1L, 3, true);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetMatchScore_Success() {
        when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant));
        when(jobListingRepository.findById(1L)).thenReturn(Optional.of(jobListing));
        when(recruitmentService.calculateMatchScore(applicant, jobListing)).thenReturn(85.0);

        ResponseEntity<Double> response = recruitmentController.getMatchScore(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(85.0, response.getBody());
    }

    @Test
    void testGetMatchScore_NotFound() {
        when(applicantRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Double> response = recruitmentController.getMatchScore(1L, 1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testBulkUpdateStatuses_Success() {
        RecruitmentController.BulkStatusUpdateRequest request =
                new RecruitmentController.BulkStatusUpdateRequest(Arrays.asList(1L, 2L), "APPROVED");

        ResponseEntity<Void> response = recruitmentController.bulkUpdateStatuses(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(recruitmentService, times(1)).bulkUpdateCandidateStatuses(request.getApplicantIds(), request.getNewStatus());
    }

    @Test
    void testBulkUpdateStatuses_BadRequest_EmptyFields() {
        RecruitmentController.BulkStatusUpdateRequest request =
                new RecruitmentController.BulkStatusUpdateRequest(Collections.emptyList(), "");

        ResponseEntity<Void> response = recruitmentController.bulkUpdateStatuses(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
