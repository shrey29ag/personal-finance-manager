package com.example.finance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Body of {@code PUT /api/transactions/{id}}. Every field is optional. A {@code date} field is not part of
 * this DTO, so a date sent by the client is silently ignored: transaction dates cannot be changed.
 *
 * @param amount      new positive amount, or null to keep
 * @param category    new category name, or null to keep
 * @param description new description, or null to keep
 */
public record UpdateTransactionRequest(
        @DecimalMin(value = "0", inclusive = false, message = "must be greater than 0") BigDecimal amount,
        String category,
        @Size(max = 255, message = "must be at most 255 characters") String description) {
}
