package com.example.finance.service;

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
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Savings goal CRUD and live progress calculation.
 */
@Service
public class SavingsGoalService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final SavingsGoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    /**
     * Creates the service using the system clock.
     *
     * @param goalRepository        goal repository
     * @param transactionRepository transaction repository (for progress sums)
     */
    @Autowired
    public SavingsGoalService(SavingsGoalRepository goalRepository, TransactionRepository transactionRepository) {
        this(goalRepository, transactionRepository, Clock.systemDefaultZone());
    }

    /**
     * Creates the service with an explicit clock (for tests).
     *
     * @param goalRepository        goal repository
     * @param transactionRepository transaction repository
     * @param clock                 clock that defines "today"
     */
    public SavingsGoalService(SavingsGoalRepository goalRepository, TransactionRepository transactionRepository,
                              Clock clock) {
        this.goalRepository = goalRepository;
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    /**
     * Creates a goal. The start date defaults to today.
     *
     * @param user    current user
     * @param request goal data
     * @return the created goal with progress
     * @throws BadRequestException if the start date is not before the target date
     */
    @Transactional
    public GoalResponse create(User user, CreateGoalRequest request) {
        LocalDate startDate = request.startDate() != null ? request.startDate() : LocalDate.now(clock);
        requireStartBeforeTarget(startDate, request.targetDate());
        SavingsGoal goal = new SavingsGoal(request.goalName().trim(), Money.normalize(request.targetAmount()),
                request.targetDate(), startDate, user);
        return toResponse(goalRepository.save(goal));
    }

    /**
     * Lists the user's goals.
     *
     * @param user current user
     * @return goals with progress
     */
    @Transactional(readOnly = true)
    public List<GoalResponse> list(User user) {
        return goalRepository.findByUserIdOrderByIdAsc(user.getId()).stream().map(this::toResponse).toList();
    }

    /**
     * Gets one goal.
     *
     * @param user current user
     * @param id   goal id
     * @return the goal with progress
     * @throws ResourceNotFoundException if the user has no such goal
     */
    @Transactional(readOnly = true)
    public GoalResponse get(User user, Long id) {
        return toResponse(findOwned(user, id));
    }

    /**
     * Updates the target amount and/or target date.
     *
     * @param user    current user
     * @param id      goal id
     * @param request fields to change
     * @return the updated goal with progress
     * @throws ResourceNotFoundException if the user has no such goal
     * @throws BadRequestException       if the new target date is not after the start date
     */
    @Transactional
    public GoalResponse update(User user, Long id, UpdateGoalRequest request) {
        SavingsGoal goal = findOwned(user, id);
        if (request.targetAmount() != null) {
            goal.setTargetAmount(Money.normalize(request.targetAmount()));
        }
        if (request.targetDate() != null) {
            requireStartBeforeTarget(goal.getStartDate(), request.targetDate());
            goal.setTargetDate(request.targetDate());
        }
        return toResponse(goalRepository.save(goal));
    }

    /**
     * Deletes a goal.
     *
     * @param user current user
     * @param id   goal id
     * @throws ResourceNotFoundException if the user has no such goal
     */
    @Transactional
    public void delete(User user, Long id) {
        goalRepository.delete(findOwned(user, id));
    }

    private SavingsGoal findOwned(User user, Long id) {
        return goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Goal " + id + " not found"));
    }

    private static void requireStartBeforeTarget(LocalDate startDate, LocalDate targetDate) {
        if (!startDate.isBefore(targetDate)) {
            throw new BadRequestException("startDate must be before targetDate");
        }
    }

    private GoalResponse toResponse(SavingsGoal goal) {
        Long userId = goal.getUser().getId();
        BigDecimal income = orZero(transactionRepository.sumByTypeSince(userId, CategoryType.INCOME, goal.getStartDate()));
        BigDecimal expense = orZero(transactionRepository.sumByTypeSince(userId, CategoryType.EXPENSE, goal.getStartDate()));
        BigDecimal progress = income.subtract(expense);
        BigDecimal target = goal.getTargetAmount();

        double percentage = progress.multiply(HUNDRED).divide(target, 2, RoundingMode.HALF_UP).doubleValue();
        BigDecimal remaining = Money.normalize(target.subtract(progress).max(BigDecimal.ZERO));

        return new GoalResponse(goal.getId(), goal.getGoalName(), target, goal.getTargetDate(), goal.getStartDate(),
                Money.total(progress), percentage, remaining);
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
