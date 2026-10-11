package com.example.finance.dto.response;

import java.util.List;

/**
 * Body of {@code GET /api/goals}.
 *
 * @param goals the user's goals
 */
public record GoalListResponse(List<GoalResponse> goals) {
}
