package com.example.finance.dto.response;

import java.util.List;

/**
 * Body of {@code GET /api/transactions}.
 *
 * @param transactions the user's transactions, newest first
 */
public record TransactionListResponse(List<TransactionResponse> transactions) {
}
