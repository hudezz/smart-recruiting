package com.recruitment.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central place to translate service-layer exceptions into HTTP responses,
 * so individual controller methods don't need their own try/catch blocks.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Missing job listing / applicant lookups become a 404 with the exception's message as the body.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // Invalid input caught in the service layer (e.g. null/blank parameters) becomes a 400.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // The Python matching microservice being down is a 503, not a 500: the rest
    // of the API still works, and the caller can retry or fall back to the
    // rule-based score.
    @ExceptionHandler(MlServiceUnavailableException.class)
    public ResponseEntity<String> handleMlServiceUnavailable(MlServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }
}
