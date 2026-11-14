package com.recruitment.api.client;

import com.recruitment.api.exception.MlServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Talks to the Python matching microservice in {@code matching_service.py},
 * which scores a resume against a job description with TF-IDF and cosine
 * similarity.
 *
 * The wire contract is the one that service actually implements: a POST to
 * {@code /match-score} with {@code job_description} and
 * {@code applicant_resume}, answered with {@code match_score} as a percentage
 * between 0 and 100. (The earlier field names here — {@code job_text},
 * {@code resume_text} — and the {@code /score} path on port 8000 did not match
 * anything on the Python side.)
 *
 * The base URL comes from {@code ml.service.base-url}, so the service can move
 * without a recompile.
 */
@Component
public class PythonMlClient {

    private static final Logger log = LoggerFactory.getLogger(PythonMlClient.class);

    private static final String SCORE_PATH = "/match-score";

    private final RestClient restClient;

    public PythonMlClient(RestClient.Builder restClientBuilder,
                          @Value("${ml.service.base-url:http://localhost:5000}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    /** Request body the Python service expects. */
    public record MatchRequest(String job_description, String applicant_resume) {}

    /** Response body the Python service returns. */
    public record MatchResponse(double match_score) {}

    /**
     * Returns how closely a resume matches a job description, as a percentage
     * from 0 to 100.
     *
     * @throws IllegalArgumentException     if either text is missing or blank —
     *                                      there is nothing to compare, and the
     *                                      Python service would fail on it
     * @throws MlServiceUnavailableException if the service cannot be reached, or
     *                                      answers with an error or an
     *                                      unreadable body
     */
    public double fetchSimilarityScore(String jobText, String resumeText) {
        if (jobText == null || jobText.isBlank()) {
            throw new IllegalArgumentException("jobText must not be blank");
        }
        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalArgumentException("resumeText must not be blank");
        }

        MatchRequest payload = new MatchRequest(jobText, resumeText);

        MatchResponse response;
        try {
            response = restClient.post()
                    .uri(SCORE_PATH)
                    .body(payload)
                    .retrieve()
                    .body(MatchResponse.class);
        } catch (RestClientException ex) {
            // Covers connection refused, timeouts, 4xx/5xx from the service and
            // a body that will not deserialize. The caller decides whether to
            // degrade or surface it; nothing here invents a score.
            log.warn("Matching service call to {} failed: {}", SCORE_PATH, ex.getMessage());
            throw new MlServiceUnavailableException(
                    "Could not reach the matching service: " + ex.getMessage(), ex);
        }

        if (response == null) {
            throw new MlServiceUnavailableException("Matching service returned an empty body");
        }

        double score = response.match_score();
        if (score < 0.0 || score > 100.0) {
            throw new MlServiceUnavailableException(
                    "Matching service returned an out-of-range score: " + score);
        }
        return score;
    }
}
