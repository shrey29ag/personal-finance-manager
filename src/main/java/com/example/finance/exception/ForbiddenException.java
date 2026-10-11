package com.example.finance.exception;

/**
 * Thrown when the user may not perform an operation, e.g. deleting a default category (HTTP 403).
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message descriptive message
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
