package com.example.finance.dto.response;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Income and expenses per category for one month.
 *
 * @param month         month (1-12)
 * @param year          year
 * @param totalIncome   income per category name
 * @param totalExpenses expenses per category name
 * @param netSavings    total income minus total expenses ({@code 0} when zero)
 */
public record MonthlyReportResponse(int month, int year, Map<String, BigDecimal> totalIncome,
                                    Map<String, BigDecimal> totalExpenses, BigDecimal netSavings) {
}
