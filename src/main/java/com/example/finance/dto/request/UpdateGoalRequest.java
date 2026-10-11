package com.example.finance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Body of {@code PUT /api/goals/{id}}. Both fields are optional.
 *
 * @param targetAmount new positive target amount, or null to keep
 * @param targetDate   new target date in the future, or null to keep
 */
public record UpdateGoalRequest(
        @DecimalMin(value = "0", inclusive = false, message = "must be greater than 0") BigDecimal targetAmount,
        @Future(message = "must be in the future") LocalDate targetDate) {
}
