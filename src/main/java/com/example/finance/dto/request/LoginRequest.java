package com.example.finance.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Body of {@code POST /api/auth/login}.
 *
 * @param username email address
 * @param password plain-text password
 */
public record LoginRequest(
        @NotBlank(message = "is required") String username,
        @NotBlank(message = "is required") String password) {
}
