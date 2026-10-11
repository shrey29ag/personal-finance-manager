package com.example.finance.service;

import com.example.finance.dto.response.MonthlyReportResponse;
import com.example.finance.dto.response.YearlyReportResponse;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.Transaction;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Monthly and yearly income/expense reports, computed from the user's transactions.
 */
@Service
public class ReportService {

    private static final int MIN_YEAR = 1;
    private static final int MAX_YEAR = 9999;

    private final TransactionRepository transactionRepository;

    /**
     * Creates the service.
     *
     * @param transactionRepository transaction repository
     */
    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Builds the report for one month.
     *
     * @param user  current user
     * @param year  year
     * @param month month (1-12)
     * @return the report
     * @throws BadRequestException if the year or month is out of range
     */
    @Transactional(readOnly = true)
    public MonthlyReportResponse monthly(User user, int year, int month) {
        validateYear(year);
        if (month < 1 || month > 12) {
            throw new BadRequestException("month must be between 1 and 12");
        }
        YearMonth ym = YearMonth.of(year, month);
        Totals totals = totals(user, ym.atDay(1), ym.atEndOfMonth());
        return new MonthlyReportResponse(month, year, totals.income, totals.expenses, totals.net());
    }

    /**
     * Builds the report for one year.
     *
     * @param user current user
     * @param year year
     * @return the report
     * @throws BadRequestException if the year is out of range
     */
    @Transactional(readOnly = true)
    public YearlyReportResponse yearly(User user, int year) {
        validateYear(year);
        Totals totals = totals(user, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        return new YearlyReportResponse(year, totals.income, totals.expenses, totals.net());
    }

    private static void validateYear(int year) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new BadRequestException("year must be between " + MIN_YEAR + " and " + MAX_YEAR);
        }
    }

    private Totals totals(User user, LocalDate from, LocalDate to) {
        List<Transaction> transactions = transactionRepository.findInRange(user.getId(), from, to);
        Totals totals = new Totals();
        for (Transaction t : transactions) {
            Map<String, BigDecimal> target =
                    t.getCategory().getType() == CategoryType.INCOME ? totals.income : totals.expenses;
            target.merge(t.getCategory().getName(), t.getAmount(), BigDecimal::add);
        }
        totals.income.replaceAll((k, v) -> Money.normalize(v));
        totals.expenses.replaceAll((k, v) -> Money.normalize(v));
        return totals;
    }

    /** Per-category sums for a period. */
    private static final class Totals {
        private final Map<String, BigDecimal> income = new LinkedHashMap<>();
        private final Map<String, BigDecimal> expenses = new LinkedHashMap<>();

        private BigDecimal net() {
            BigDecimal in = income.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal out = expenses.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            return Money.total(in.subtract(out));
        }
    }
}
