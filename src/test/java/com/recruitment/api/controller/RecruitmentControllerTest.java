package com.recruitment.api.controller;

import com.recruitment.api.dto.ApplicantDto;
import com.recruitment.api.dto.JobListingDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecruitmentControllerTest {

    @Mock
    private RecruitmentService recruitmentService;

    @InjectMocks
    private RecruitmentController recruitmentController;

    private ApplicantDto applicantDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        JobListingDto jobListingDto = JobListingDto.builder()
                .id(1L)
                .jobName("Software Engineer")
                .jobDescription("Description")
                .minimumWorkExperience(3)
                .requiredCertification(true)
                .build();
        applicantDto = ApplicantDto.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .phoneNumber("123")
                .nationalId("NID1")
                .yearsOfExperience(4)
                .hasCertification(true)
                .status("PENDING")
                .jobListing(jobListingDto)
                .resumeText("Resume Content")
                .build();
    }

    @Test
    void testFilterApplications_Success() {
        // TODO: Update or implement mock verification after implementing controller logic
        when(recruitmentService.filterApplications(1L, 3, true))
                .thenReturn(Collections.singletonList(applicantDto));

        ResponseEntity<List<ApplicantDto>> response = recruitmentController.filterApplications(1L, 3, true);

        // Note: Change assertion based on your implementation
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testFilterApplications_BadRequest_InvalidId() {
        ResponseEntity<List<ApplicantDto>> response = recruitmentController.filterApplications(-1L, 3, true);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetMatchScore_Success() {
        when(recruitmentService.calculateMatchScore(1L, 1L)).thenReturn(85.0);

        ResponseEntity<Double> response = recruitmentController.getMatchScore(1L, 1L);

        // Note: Change assertion based on your implementation
        assertEquals(HttpStatus.OK, response.getStatusCode());
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
