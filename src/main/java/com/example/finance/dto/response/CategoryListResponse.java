package com.example.finance.dto.response;

import java.util.List;

/**
 * Body of {@code GET /api/categories}.
 *
 * @param categories default categories plus the user's custom categories
 */
public record CategoryListResponse(List<CategoryResponse> categories) {
}
