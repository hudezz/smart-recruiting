package com.recruitment.api.service;

import com.recruitment.api.dto.ApplicantDto;
import com.recruitment.api.exception.ResourceNotFoundException;
import com.recruitment.api.model.Applicant;
import com.recruitment.api.model.JobListing;
import com.recruitment.api.repository.ApplicantRepository;
import com.recruitment.api.repository.JobListingRepository;
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

// Repositories are mocked throughout: these are unit tests of the service's
// filtering/scoring/validation logic, not integration tests of the database.
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
        Long jobListingId = 1L;
        JobListing jobListing = new JobListing(jobListingId, "Software Engineer", "Description", 3, true);

        Applicant app1 = new Applicant(1L, "Alice", "Smith", "alice@example.com", "pass", "123", "NID1", 4, true, jobListing, "PENDING");
        Applicant app2 = new Applicant(2L, "Bob", "Jones", "bob@example.com", "pass", "456", "NID2", 2, true, jobListing, "PENDING");
        Applicant app3 = new Applicant(3L, "Charlie", "Brown", "charlie@example.com", "pass", "789", "NID3", 5, false, jobListing, "PENDING");

        when(jobListingRepository.existsById(jobListingId)).thenReturn(true);
        when(applicantRepository.findByJobListingId(jobListingId)).thenReturn(Arrays.asList(app1, app2, app3));

        // minExperience = 3, requiresCertification = true:
        // Bob has < 3 years -> excluded. Charlie has no certification -> excluded. Only Alice qualifies.
        List<ApplicantDto> result = recruitmentService.filterApplications(jobListingId, 3, true);

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getFirstName());
    }

    @Test
    void testFilterApplications_NoCertificationRequired_IncludesUncertifiedApplicants() {
        Long jobListingId = 1L;
        JobListing jobListing = new JobListing(jobListingId, "Software Engineer", "Description", 3, false);
        Applicant app1 = new Applicant(1L, "Alice", "Smith", "alice@example.com", "pass", "123", "NID1", 4, true, jobListing, "PENDING");
        Applicant app3 = new Applicant(3L, "Charlie", "Brown", "charlie@example.com", "pass", "789", "NID3", 5, false, jobListing, "PENDING");

        when(jobListingRepository.existsById(jobListingId)).thenReturn(true);
        when(applicantRepository.findByJobListingId(jobListingId)).thenReturn(Arrays.asList(app1, app3));

        List<ApplicantDto> result = recruitmentService.filterApplications(jobListingId, 3, false);

        assertEquals(2, result.size());
    }

    // Filtering against a listing id that doesn't exist should fail loud (404 at
    // the controller level), not silently return an empty list.
    @Test
    void testFilterApplications_JobListingNotFound_ThrowsException() {
        when(jobListingRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> recruitmentService.filterApplications(99L, 3, true));
    }

    @Test
    void testFilterApplications_NullJobListingId_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> recruitmentService.filterApplications(null, 3, true));
    }

    @Test
    void testCalculateMatchScore_PerfectMatch() {
        Long applicantId = 1L;
        Long jobListingId = 1L;
        JobListing jobListing = new JobListing(jobListingId, "Software Engineer", "Description", 3, true);
        Applicant applicant = new Applicant(applicantId, "Alice", "Smith", "alice@example.com", "pass", "123", "NID1", 5, true, jobListing, "PENDING");

        when(applicantRepository.findById(applicantId)).thenReturn(Optional.of(applicant));
        when(jobListingRepository.findById(jobListingId)).thenReturn(Optional.of(jobListing));

        double score = recruitmentService.calculateMatchScore(applicantId, jobListingId);

        assertEquals(100.0, score);
    }

    @Test
    void testCalculateMatchScore_PartialExperienceNoCertification() {
        Long applicantId = 1L;
        Long jobListingId = 1L;
        JobListing jobListing = new JobListing(jobListingId, "Software Engineer", "Description", 4, true);
        Applicant applicant = new Applicant(applicantId, "Bob", "Jones", "bob@example.com", "pass", "456", "NID2", 2, false, jobListing, "PENDING");

        when(applicantRepository.findById(applicantId)).thenReturn(Optional.of(applicant));
        when(jobListingRepository.findById(jobListingId)).thenReturn(Optional.of(jobListing));

        // Experience: 2/4 * 70 = 35. Certification required but missing: 0. Total = 35.
        double score = recruitmentService.calculateMatchScore(applicantId, jobListingId);

        assertEquals(35.0, score);
    }

    @Test
    void testCalculateMatchScore_ApplicantNotFound_ThrowsException() {
        when(applicantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> recruitmentService.calculateMatchScore(1L, 1L));
    }

    // Confirms the status mutation happens in-memory on the fetched entities
    // before saveAll persists them — not just that saveAll was called.
    @Test
    void testBulkUpdateCandidateStatuses_Success() {
        Applicant app1 = new Applicant(1L, "Alice", "Smith", "alice@example.com", "pass", "123", "NID1", 4, true, null, "PENDING");
        Applicant app2 = new Applicant(2L, "Bob", "Jones", "bob@example.com", "pass", "456", "NID2", 2, true, null, "PENDING");
        List<Long> ids = Arrays.asList(1L, 2L);

        when(applicantRepository.findAllById(ids)).thenReturn(Arrays.asList(app1, app2));

        recruitmentService.bulkUpdateCandidateStatuses(ids, "APPROVED");

        assertEquals("APPROVED", app1.getStatus());
        assertEquals("APPROVED", app2.getStatus());
        verify(applicantRepository, times(1)).saveAll(Arrays.asList(app1, app2));
    }

    @Test
    void testBulkUpdateCandidateStatuses_EmptyIds_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> recruitmentService.bulkUpdateCandidateStatuses(Collections.emptyList(), "APPROVED"));
    }

    @Test
    void testBulkUpdateCandidateStatuses_BlankStatus_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> recruitmentService.bulkUpdateCandidateStatuses(Arrays.asList(1L, 2L), " "));
    }
}
