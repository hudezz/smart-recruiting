package com.recruitment.api.exception;

/**
 * Raised when the Python matching microservice cannot be reached or answers
 * with something unusable. Translated to HTTP 503 by
 * {@link GlobalExceptionHandler}, so a caller can tell "the similarity service
 * is down" apart from "the similarity is zero".
 */
public class MlServiceUnavailableException extends RuntimeException {

    public MlServiceUnavailableException(String message) {
        super(message);
    }

    public MlServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
