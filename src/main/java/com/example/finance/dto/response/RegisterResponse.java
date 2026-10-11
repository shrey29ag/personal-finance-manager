package com.example.finance.dto.response;

/**
 * Body returned after a successful registration.
 *
 * @param message confirmation text
 * @param userId  id of the new user
 */
public record RegisterResponse(String message, Long userId) {
}
