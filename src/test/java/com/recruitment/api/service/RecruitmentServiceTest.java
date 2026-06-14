package com.recruitment.api.service;

import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;
import com.recruitment.api.repository.ApplicantRepository;
import com.recruitment.api.repository.JobListingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecruitmentServiceTest {

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private JobListingRepository jobListingRepository;

    @InjectMocks
    private RecruitmentService recruitmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFilterApplications_Success() {
        // Arrange
        Long jobListingId = 1L;
        JobListing jobListing = new JobListing(jobListingId, "Software Engineer", "Description", 3, true);
        
        Applicant app1 = new Applicant(1L, "Alice", "Smith", "alice@example.com", "pass", "123", "NID1", 4, true, jobListing, "PENDING");
        Applicant app2 = new Applicant(2L, "Bob", "Jones", "bob@example.com", "pass", "456", "NID2", 2, true, jobListing, "PENDING"); // insufficient experience
        Applicant app3 = new Applicant(3L, "Charlie", "Brown", "charlie@example.com", "pass", "789", "NID3", 5, false, jobListing, "PENDING"); // no cert

        when(jobListingRepository.findById(jobListingId)).thenReturn(Optional.of(jobListing));
        when(applicantRepository.findByJobListingId(jobListingId)).thenReturn(Arrays.asList(app1, app2, app3));

        // Act
        List<Applicant> result = recruitmentService.filterApplications(jobListingId, 3, true);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getFirstName());
    }

    @Test
    void testFilterApplications_NullJobListingId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            recruitmentService.filterApplications(null, 3, true);
        });
    }

    @Test
    void testFilterApplications_JobListingNotFound_ThrowsException() {
        when(jobListingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            recruitmentService.filterApplications(1L, 3, true);
        });
    }

    @Test
    void testCalculateMatchScore_PerfectMatch() {
        // Arrange
        JobListing jobListing = new JobListing(1L, "Software Engineer", "Description", 3, true);
        Applicant applicant = new Applicant(1L, "Alice", "Smith", "alice@example.com", "pass", "123", "NID1", 4, true, jobListing, "PENDING");

        // Act
        double score = recruitmentService.calculateMatchScore(applicant, jobListing);

        // Assert
        assertEquals(100.0, score);
    }

    @Test
    void testCalculateMatchScore_NoCertNoExperience() {
        // Arrange
        JobListing jobListing = new JobListing(1L, "Software Engineer", "Description", 3, true);
        Applicant applicant = new Applicant(1L, "Bob", "Jones", "bob@example.com", "pass", "456", "NID2", 2, false, jobListing, "PENDING");

        // Act
        double score = recruitmentService.calculateMatchScore(applicant, jobListing);

        // Assert
        assertEquals(0.0, score);
    }

    @Test
    void testBulkUpdateCandidateStatuses_Success() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L);
        Applicant app1 = new Applicant(1L, "Alice", "Smith", "alice@example.com", "pass", "123", "NID1", 4, true, null, "PENDING");
        Applicant app2 = new Applicant(2L, "Bob", "Jones", "bob@example.com", "pass", "456", "NID2", 2, false, null, "PENDING");
        List<Applicant> applicants = Arrays.asList(app1, app2);

        when(applicantRepository.findAllById(ids)).thenReturn(applicants);

        // Act
        recruitmentService.bulkUpdateCandidateStatuses(ids, "APPROVED");

        // Assert
        assertEquals("APPROVED", app1.getStatus());
        assertEquals("APPROVED", app2.getStatus());
        verify(applicantRepository, times(1)).saveAll(applicants);
    }

    @Test
    void testBulkUpdateCandidateStatuses_EmptyIds_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            recruitmentService.bulkUpdateCandidateStatuses(Collections.emptyList(), "APPROVED");
        });
    }
}
