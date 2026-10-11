package com.example.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A savings goal with its live progress.
 *
 * @param id                 goal id
 * @param goalName           name
 * @param targetAmount       target, scale 2
 * @param targetDate         target date
 * @param startDate          start date
 * @param currentProgress    income minus expenses since the start date ({@code 0} when zero)
 * @param progressPercentage progress as a percentage of the target, 2 decimals
 * @param remainingAmount    amount still needed, never negative, scale 2
 */
public record GoalResponse(Long id, String goalName, BigDecimal targetAmount, LocalDate targetDate,
                           LocalDate startDate, BigDecimal currentProgress, double progressPercentage,
                           BigDecimal remainingAmount) {
}
