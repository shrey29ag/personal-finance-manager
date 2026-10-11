package com.example.finance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Body of {@code POST /api/goals}.
 *
 * @param goalName     name of the goal
 * @param targetAmount positive target amount
 * @param targetDate   target date, strictly in the future
 * @param startDate    optional start date (defaults to today); must be before the target date
 */
public record CreateGoalRequest(
        @NotBlank(message = "is required") @Size(max = 100, message = "must be at most 100 characters") String goalName,
        @NotNull(message = "is required")
        @DecimalMin(value = "0", inclusive = false, message = "must be greater than 0") BigDecimal targetAmount,
        @NotNull(message = "is required") @Future(message = "must be in the future") LocalDate targetDate,
        LocalDate startDate) {
}
