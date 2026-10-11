package com.example.finance.exception;

/**
 * Thrown when a request conflicts with existing data, e.g. a duplicate username or category (HTTP 409).
 */
public class ConflictException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message descriptive message
     */
    public ConflictException(String message) {
        super(message);
    }
}
