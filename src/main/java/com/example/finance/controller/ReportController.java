package com.example.finance.controller;

import com.example.finance.dto.response.MonthlyReportResponse;
import com.example.finance.dto.response.YearlyReportResponse;
import com.example.finance.service.CurrentUserService;
import com.example.finance.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Report endpoints.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final CurrentUserService currentUserService;

    /**
     * Creates the controller.
     *
     * @param reportService      report service
     * @param currentUserService current-user lookup
     */
    public ReportController(ReportService reportService, CurrentUserService currentUserService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    /**
     * Monthly report.
     *
     * @param year  year
     * @param month month (1-12)
     * @return income/expenses per category and net savings
     */
    @GetMapping("/monthly/{year}/{month}")
    public MonthlyReportResponse monthly(@PathVariable int year, @PathVariable int month) {
        return reportService.monthly(currentUserService.getCurrentUser(), year, month);
    }

    /**
     * Yearly report.
     *
     * @param year year
     * @return income/expenses per category and net savings
     */
    @GetMapping("/yearly/{year}")
    public YearlyReportResponse yearly(@PathVariable int year) {
        return reportService.yearly(currentUserService.getCurrentUser(), year);
    }
}
