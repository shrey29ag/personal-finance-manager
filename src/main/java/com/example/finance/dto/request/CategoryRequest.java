package com.example.finance.dto.request;

import com.example.finance.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /api/categories}.
 *
 * @param name category name
 * @param type INCOME or EXPENSE
 */
public record CategoryRequest(
        @NotBlank(message = "is required") @Size(max = 100, message = "must be at most 100 characters") String name,
        @NotNull(message = "is required (INCOME or EXPENSE)") CategoryType type) {
}
