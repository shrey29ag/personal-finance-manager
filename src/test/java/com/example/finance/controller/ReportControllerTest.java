package com.example.finance.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.finance.TestData;
import com.example.finance.dto.response.MonthlyReportResponse;
import com.example.finance.dto.response.YearlyReportResponse;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.security.CustomUserDetailsService;
import com.example.finance.service.CurrentUserService;
import com.example.finance.service.ReportService;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
@WithApiSecurity
@WithMockUser
class ReportControllerTest {

    private final User user = TestData.user(1L, "a@example.com");

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private ReportService reportService;
    @MockitoBean
    private CurrentUserService currentUserService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void monthlyReportExactJson() throws Exception {
        Map<String, BigDecimal> income = new LinkedHashMap<>();
        income.put("Salary", new BigDecimal("5500.00"));
        income.put("Freelance", new BigDecimal("1500.00"));
        when(reportService.monthly(user, 2024, 1)).thenReturn(new MonthlyReportResponse(1, 2024, income,
                Map.of("Food", new BigDecimal("450.00")), new BigDecimal("6550.00")));

        mvc.perform(get("/api/reports/monthly/2024/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"month\":1,\"year\":2024,"
                        + "\"totalIncome\":{\"Salary\":5500.00,\"Freelance\":1500.00},"
                        + "\"totalExpenses\":{\"Food\":450.00},\"netSavings\":6550.00}"));
    }

    @Test
    void emptyMonthHasPlainZeroNetSavings() throws Exception {
        when(reportService.monthly(user, 2024, 12))
                .thenReturn(new MonthlyReportResponse(12, 2024, Map.of(), Map.of(), BigDecimal.ZERO));

        mvc.perform(get("/api/reports/monthly/2024/12"))
                .andExpect(content().string("{\"month\":12,\"year\":2024,\"totalIncome\":{},"
                        + "\"totalExpenses\":{},\"netSavings\":0}"));
    }

    @Test
    void invalidMonthIs400() throws Exception {
        when(reportService.monthly(user, 2024, 13)).thenThrow(new BadRequestException("month must be between 1 and 12"));

        mvc.perform(get("/api/reports/monthly/2024/13")).andExpect(status().isBadRequest());
    }

    @Test
    void nonNumericMonthIs400() throws Exception {
        mvc.perform(get("/api/reports/monthly/2024/jan")).andExpect(status().isBadRequest());
    }

    @Test
    void yearlyReportExactJson() throws Exception {
        when(reportService.yearly(user, 2023))
                .thenReturn(new YearlyReportResponse(2023, Map.of(), Map.of(), BigDecimal.ZERO));

        mvc.perform(get("/api/reports/yearly/2023"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"year\":2023,\"totalIncome\":{},\"totalExpenses\":{},\"netSavings\":0}"));
    }

    @Test
    @WithAnonymousUser
    void withoutSessionIs401() throws Exception {
        mvc.perform(get("/api/reports/yearly/2024")).andExpect(status().isUnauthorized());
    }
}
