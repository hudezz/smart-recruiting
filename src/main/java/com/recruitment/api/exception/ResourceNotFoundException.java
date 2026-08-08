package com.recruitment.api.exception;

/**
 * Thrown when a lookup (job listing, applicant, etc.) doesn't resolve to an
 * existing record. Caught by {@link GlobalExceptionHandler} and turned into a
 * 404 response, so callers get a clear "not found" instead of a stack trace
 * or a silently empty result.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
