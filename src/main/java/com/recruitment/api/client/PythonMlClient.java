package com.recruitment.api.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PythonMlClient {

    private final RestClient restClient;

    public PythonMlClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://localhost:8000").build();
    }

    public record MatchRequest(String job_text, String resume_text) {}
    public record MatchResponse(double match_score) {}

    /**
     * Fetches similarity score from the Python FastAPI ML microservice.
     */
    public double fetchSimilarityScore(String jobText, String resumeText) {
        // TODO 1: Construct the MatchRequest payload with jobText and resumeText.
        // TODO 2: Perform an HTTP POST request to "/score" endpoint of the Python FastAPI server.
        // TODO 3: Handle HTTP error status codes or communication failures (e.g., fallback, throw custom exception).
        // TODO 4: Extract the match_score from the MatchResponse payload.
        // TODO 5: Return the similarity score as a double.
        return 0.0;
    }
}
