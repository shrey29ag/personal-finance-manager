package com.example.finance.exception;

/**
 * Thrown when a requested resource does not exist or is not owned by the current user (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message descriptive message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
