package com.example.finance.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.finance.TestData;
import com.example.finance.dto.response.GoalResponse;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.security.CustomUserDetailsService;
import com.example.finance.service.CurrentUserService;
import com.example.finance.service.SavingsGoalService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SavingsGoalController.class)
@WithApiSecurity
@WithMockUser
class SavingsGoalControllerTest {

    private final User user = TestData.user(1L, "a@example.com");

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private SavingsGoalService goalService;
    @MockitoBean
    private CurrentUserService currentUserService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentUser()).thenReturn(user);
    }

    private static GoalResponse goal(String progress, double pct, String remaining) {
        return new GoalResponse(2L, "Emergency Fund", new BigDecimal("10000.00"), LocalDate.of(2027, 1, 1),
                LocalDate.of(2024, 1, 1), new BigDecimal(progress), pct, new BigDecimal(remaining));
    }

    private static String createBody(String amount, String targetDate) {
        return "{\"goalName\":\"G\",\"targetAmount\":" + amount + ",\"targetDate\":\"" + targetDate + "\"}";
    }

    @Test
    void createReturns201WithExactJson() throws Exception {
        when(goalService.create(eq(user), any())).thenReturn(goal("6550.00", 65.5, "3450.00"));

        mvc.perform(post("/api/goals").contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("10000.00", "2027-01-01")))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"id\":2,\"goalName\":\"Emergency Fund\",\"targetAmount\":10000.00,"
                        + "\"targetDate\":\"2027-01-01\",\"startDate\":\"2024-01-01\",\"currentProgress\":6550.00,"
                        + "\"progressPercentage\":65.5,\"remainingAmount\":3450.00}"));
    }

    @Test
    void zeroProgressSerializesAsPlainZeroAndZeroPointZero() throws Exception {
        GoalResponse zero = new GoalResponse(3L, "Vacation", new BigDecimal("5000.00"), LocalDate.of(2027, 12, 1),
                LocalDate.of(2024, 2, 1), BigDecimal.ZERO, 0.0, new BigDecimal("5000.00"));
        when(goalService.get(user, 3L)).thenReturn(zero);

        mvc.perform(get("/api/goals/3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "\"currentProgress\":0,\"progressPercentage\":0.0,")));
    }

    @Test
    void createWithPastTargetDateIs400() throws Exception {
        mvc.perform(post("/api/goals").contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("5000.00", "2023-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("targetDate: must be in the future"));
        verify(goalService, never()).create(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1000.00"})
    void createWithZeroOrNegativeTargetIs400(String amount) throws Exception {
        mvc.perform(post("/api/goals").contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(amount, "2099-01-01")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithStartAfterTargetIs400() throws Exception {
        when(goalService.create(eq(user), any())).thenThrow(new BadRequestException("startDate must be before targetDate"));

        mvc.perform(post("/api/goals").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goalName\":\"G\",\"targetAmount\":5000,\"targetDate\":\"2099-01-01\",\"startDate\":\"2100-01-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listWrapsGoals() throws Exception {
        when(goalService.list(user)).thenReturn(List.of(goal("0", 0.0, "10000.00")));

        mvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals.length()").value(1));
    }

    @Test
    void emptyListSerializesAsEmptyArray() throws Exception {
        when(goalService.list(user)).thenReturn(List.of());

        mvc.perform(get("/api/goals")).andExpect(content().string("{\"goals\":[]}"));
    }

    @Test
    void updateReturnsRecalculatedProgress() throws Exception {
        when(goalService.update(eq(user), eq(2L), any())).thenReturn(goal("9050.00", 60.33, "5950.00"));

        mvc.perform(put("/api/goals/2").contentType(MediaType.APPLICATION_JSON).content("{\"targetAmount\":15000.00}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"progressPercentage\":60.33,")));
    }

    @Test
    void updateWithPastTargetDateIs400() throws Exception {
        mvc.perform(put("/api/goals/2").contentType(MediaType.APPLICATION_JSON).content("{\"targetDate\":\"2020-01-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void otherUsersGoalIs404() throws Exception {
        when(goalService.get(user, 9L)).thenThrow(new ResourceNotFoundException("nope"));
        when(goalService.update(eq(user), eq(9L), any())).thenThrow(new ResourceNotFoundException("nope"));
        doThrow(new ResourceNotFoundException("nope")).when(goalService).delete(user, 9L);

        mvc.perform(get("/api/goals/9")).andExpect(status().isNotFound());
        mvc.perform(put("/api/goals/9").contentType(MediaType.APPLICATION_JSON).content("{\"targetAmount\":1}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/goals/9")).andExpect(status().isNotFound());
    }

    @Test
    void deleteReturnsMessage() throws Exception {
        mvc.perform(delete("/api/goals/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Goal deleted successfully\"}"));
        verify(goalService).delete(user, 2L);
    }

    @Test
    void nonNumericIdIs400() throws Exception {
        mvc.perform(get("/api/goals/abc")).andExpect(status().isBadRequest());
    }
}
