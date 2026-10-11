package com.example.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.finance.TestData;
import com.example.finance.dto.request.CreateGoalRequest;
import com.example.finance.dto.request.UpdateGoalRequest;
import com.example.finance.dto.response.GoalResponse;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.SavingsGoal;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.SavingsGoalRepository;
import com.example.finance.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 11);

    private final User user = TestData.user(1L, "a@example.com");

    @Mock
    private SavingsGoalRepository goalRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private SavingsGoalService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-11T10:00:00Z"), ZoneOffset.UTC);
        service = new SavingsGoalService(goalRepository, transactionRepository, clock);
    }

    private void sums(LocalDate since, String income, String expense) {
        when(transactionRepository.sumByTypeSince(1L, CategoryType.INCOME, since))
                .thenReturn(income == null ? null : new BigDecimal(income));
        when(transactionRepository.sumByTypeSince(1L, CategoryType.EXPENSE, since))
                .thenReturn(expense == null ? null : new BigDecimal(expense));
    }

    private void saveReturnsArgumentWithId(long id) {
        when(goalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> {
            SavingsGoal g = inv.getArgument(0);
            g.setId(id);
            return g;
        });
    }

    @Test
    void createComputesProgressPercentageAndRemaining() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        saveReturnsArgumentWithId(2L);
        sums(start, "7000.00", "450.00");

        GoalResponse response = service.create(user, new CreateGoalRequest("Emergency Fund",
                new BigDecimal("10000"), LocalDate.of(2027, 1, 1), start));

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.goalName()).isEqualTo("Emergency Fund");
        assertThat(response.targetAmount()).hasToString("10000.00");
        assertThat(response.currentProgress()).hasToString("6550.00");
        assertThat(response.progressPercentage()).isEqualTo(65.5);
        assertThat(response.remainingAmount()).hasToString("3450.00");
        assertThat(response.startDate()).isEqualTo(start);
        assertThat(response.targetDate()).isEqualTo(LocalDate.of(2027, 1, 1));
    }

    @Test
    void createDefaultsStartDateToToday() {
        saveReturnsArgumentWithId(1L);
        sums(TODAY, null, null);

        GoalResponse response = service.create(user, new CreateGoalRequest("No Start",
                new BigDecimal("10000.00"), LocalDate.of(2027, 1, 1), null));

        assertThat(response.startDate()).isEqualTo(TODAY);
    }

    @Test
    void zeroProgressUsesPlainZeroAndZeroPointZeroPercent() {
        LocalDate start = LocalDate.of(2024, 2, 1);
        saveReturnsArgumentWithId(3L);
        sums(start, null, null);

        GoalResponse response = service.create(user, new CreateGoalRequest("Vacation",
                new BigDecimal("5000.00"), LocalDate.of(2027, 12, 1), start));

        assertThat(response.currentProgress()).isSameAs(BigDecimal.ZERO).hasToString("0");
        assertThat(response.progressPercentage()).isEqualTo(0.0);
        assertThat(response.remainingAmount()).hasToString("5000.00");
    }

    @Test
    void incomeEqualToExpenseAlsoGivesPlainZero() {
        LocalDate start = LocalDate.of(2024, 2, 1);
        saveReturnsArgumentWithId(3L);
        sums(start, "500.00", "500.00");

        GoalResponse response = service.create(user, new CreateGoalRequest("Vacation",
                new BigDecimal("5000.00"), LocalDate.of(2027, 12, 1), start));

        assertThat(response.currentProgress()).hasToString("0");
    }

    @Test
    void createRejectsStartDateNotBeforeTargetDate() {
        assertThatThrownBy(() -> service.create(user, new CreateGoalRequest("Bad",
                new BigDecimal("5000"), LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 1))))
                .isInstanceOf(BadRequestException.class);
        verify(goalRepository, never()).save(any());
    }

    @Test
    void createRejectsStartDateAfterFutureTargetDate() {
        // Unlike the E2E script's case, the target date is still in the future (clock is 2026-10-11),
        // so only the start-before-target rule can reject this.
        CreateGoalRequest request = new CreateGoalRequest("Invalid Dates Goal", new BigDecimal("5000.00"),
                LocalDate.of(2027, 6, 1), LocalDate.of(2028, 1, 1));

        assertThatThrownBy(() -> service.create(user, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("startDate must be before targetDate");
        verify(goalRepository, never()).save(any());
    }

    @Test
    void updateTargetAmountRoundsPercentageToTwoDecimals() {
        SavingsGoal goal = TestData.goal(2L, "10000.00", "2024-01-01", user);
        when(goalRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(goal)).thenReturn(goal);
        sums(LocalDate.of(2024, 1, 1), "10000.00", "950.00");

        GoalResponse response = service.update(user, 2L, new UpdateGoalRequest(new BigDecimal("15000"), null));

        assertThat(response.targetAmount()).hasToString("15000.00");
        assertThat(response.progressPercentage()).isEqualTo(60.33);
        assertThat(response.remainingAmount()).hasToString("5950.00");
    }

    @Test
    void updateTargetDateValidatesAgainstStartDate() {
        SavingsGoal goal = TestData.goal(2L, "10000.00", "2024-01-01", user);
        when(goalRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(goal)).thenReturn(goal);
        sums(LocalDate.of(2024, 1, 1), null, null);

        GoalResponse response = service.update(user, 2L, new UpdateGoalRequest(null, LocalDate.of(2028, 1, 1)));
        assertThat(response.targetDate()).isEqualTo(LocalDate.of(2028, 1, 1));

        assertThatThrownBy(() -> service.update(user, 2L, new UpdateGoalRequest(null, LocalDate.of(2023, 1, 1))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void progressAboveTargetCapsRemainingAtZero() {
        SavingsGoal goal = TestData.goal(2L, "1000.00", "2024-01-01", user);
        when(goalRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(goal));
        sums(LocalDate.of(2024, 1, 1), "1500.00", null);

        GoalResponse response = service.get(user, 2L);

        assertThat(response.progressPercentage()).isEqualTo(150.0);
        assertThat(response.remainingAmount()).hasToString("0.00");
    }

    @Test
    void listReturnsAllGoalsOfUser() {
        SavingsGoal a = TestData.goal(1L, "100.00", "2024-01-01", user);
        SavingsGoal b = TestData.goal(2L, "200.00", "2024-01-01", user);
        when(goalRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(a, b));
        when(transactionRepository.sumByTypeSince(eq(1L), any(), any())).thenReturn(null);

        assertThat(service.list(user)).extracting(GoalResponse::id).containsExactly(1L, 2L);
    }

    @Test
    void otherUsersOrMissingGoalIsNotFound() {
        when(goalRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(user, 7L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.update(user, 7L, new UpdateGoalRequest(BigDecimal.TEN, null)))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.delete(user, 7L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesOwnedGoal() {
        SavingsGoal goal = TestData.goal(2L, "10000.00", "2024-01-01", user);
        when(goalRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(goal));

        service.delete(user, 2L);

        verify(goalRepository).delete(goal);
    }
}
