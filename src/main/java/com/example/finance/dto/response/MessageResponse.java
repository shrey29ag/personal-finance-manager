package com.example.finance.dto.response;

/**
 * Simple confirmation body, e.g. {@code {"message":"Logout successful"}}.
 *
 * @param message confirmation text
 */
public record MessageResponse(String message) {
}
