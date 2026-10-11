package com.example.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.finance.TestData;
import com.example.finance.dto.response.MonthlyReportResponse;
import com.example.finance.dto.response.YearlyReportResponse;
import com.example.finance.entity.Category;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private final User user = TestData.user(1L, "a@example.com");
    private final Category salary = TestData.defaultCategory(1L, "Salary", CategoryType.INCOME);
    private final Category food = TestData.defaultCategory(2L, "Food", CategoryType.EXPENSE);
    private final Category freelance = TestData.customCategory(9L, "Freelance", CategoryType.INCOME, user);

    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private ReportService service;

    @Test
    void monthlyGroupsByCategoryAndComputesNetSavings() {
        when(transactionRepository.findInRange(1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .thenReturn(List.of(
                        TestData.transaction(1L, "5000.00", "2024-01-15", salary, user),
                        TestData.transaction(2L, "500.00", "2024-01-20", salary, user),
                        TestData.transaction(3L, "1500.00", "2024-01-18", freelance, user),
                        TestData.transaction(4L, "450.00", "2024-01-17", food, user)));

        MonthlyReportResponse report = service.monthly(user, 2024, 1);

        assertThat(report.month()).isEqualTo(1);
        assertThat(report.year()).isEqualTo(2024);
        assertThat(report.totalIncome()).containsEntry("Salary", new BigDecimal("5500.00"))
                .containsEntry("Freelance", new BigDecimal("1500.00"));
        assertThat(report.totalExpenses()).containsEntry("Food", new BigDecimal("450.00"));
        assertThat(report.netSavings()).hasToString("6550.00");
    }

    @Test
    void emptyMonthHasEmptyMapsAndPlainZeroNetSavings() {
        when(transactionRepository.findInRange(1L, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29)))
                .thenReturn(List.of());

        MonthlyReportResponse report = service.monthly(user, 2024, 2);

        assertThat(report.totalIncome()).isEmpty();
        assertThat(report.totalExpenses()).isEmpty();
        assertThat(report.netSavings()).isSameAs(BigDecimal.ZERO).hasToString("0");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 13, -1})
    void invalidMonthIsBadRequest(int month) {
        assertThatThrownBy(() -> service.monthly(user, 2024, month)).isInstanceOf(BadRequestException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10000})
    void invalidYearIsBadRequest(int year) {
        assertThatThrownBy(() -> service.monthly(user, year, 1)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.yearly(user, year)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void yearlyCoversWholeYear() {
        when(transactionRepository.findInRange(1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)))
                .thenReturn(List.of(
                        TestData.transaction(1L, "5500.00", "2024-01-15", salary, user),
                        TestData.transaction(2L, "450.00", "2024-06-17", food, user)));

        YearlyReportResponse report = service.yearly(user, 2024);

        assertThat(report.year()).isEqualTo(2024);
        assertThat(report.totalIncome()).containsEntry("Salary", new BigDecimal("5500.00"));
        assertThat(report.totalExpenses()).containsEntry("Food", new BigDecimal("450.00"));
        assertThat(report.netSavings()).hasToString("5050.00");
    }

    @Test
    void yearWithNoDataHasPlainZeroNetSavings() {
        when(transactionRepository.findInRange(1L, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31)))
                .thenReturn(List.of());

        assertThat(service.yearly(user, 2023).netSavings()).hasToString("0");
    }
}
