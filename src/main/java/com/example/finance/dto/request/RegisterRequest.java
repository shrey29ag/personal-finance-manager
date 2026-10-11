package com.example.finance.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Body of {@code POST /api/auth/register}.
 *
 * @param username    email address
 * @param password    plain-text password (hashed before storage)
 * @param fullName    full name
 * @param phoneNumber phone number, optional leading '+', 7 to 15 digits
 */
public record RegisterRequest(
        @NotBlank(message = "is required") @Email(message = "must be a valid email") String username,
        @NotBlank(message = "is required") String password,
        @NotBlank(message = "is required") String fullName,
        @NotBlank(message = "is required")
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "must be 7-15 digits, optionally starting with +")
        String phoneNumber) {
}
