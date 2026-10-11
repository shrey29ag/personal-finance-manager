package com.example.finance.exception;

/**
 * Thrown when a request is invalid for business reasons not covered by bean validation (HTTP 400).
 */
public class BadRequestException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message descriptive message
     */
    public BadRequestException(String message) {
        super(message);
    }
}
