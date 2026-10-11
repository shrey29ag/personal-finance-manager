package com.example.finance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Body of {@code POST /api/transactions}.
 *
 * @param amount      positive amount
 * @param date        transaction date (YYYY-MM-DD), not in the future
 * @param category    name of a default or the user's custom category
 * @param description optional description
 */
public record CreateTransactionRequest(
        @NotNull(message = "is required")
        @DecimalMin(value = "0", inclusive = false, message = "must be greater than 0") BigDecimal amount,
        @NotNull(message = "is required") @PastOrPresent(message = "cannot be in the future") LocalDate date,
        @NotBlank(message = "is required") String category,
        @Size(max = 255, message = "must be at most 255 characters") String description) {
}
