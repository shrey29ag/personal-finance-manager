package com.example.finance.controller;

import com.example.finance.dto.request.CreateGoalRequest;
import com.example.finance.dto.request.UpdateGoalRequest;
import com.example.finance.dto.response.GoalListResponse;
import com.example.finance.dto.response.GoalResponse;
import com.example.finance.dto.response.MessageResponse;
import com.example.finance.service.CurrentUserService;
import com.example.finance.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Savings goal endpoints.
 */
@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalService goalService;
    private final CurrentUserService currentUserService;

    /**
     * Creates the controller.
     *
     * @param goalService        goal service
     * @param currentUserService current-user lookup
     */
    public SavingsGoalController(SavingsGoalService goalService, CurrentUserService currentUserService) {
        this.goalService = goalService;
        this.currentUserService = currentUserService;
    }

    /**
     * Creates a goal.
     *
     * @param request goal data
     * @return the created goal
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse create(@Valid @RequestBody CreateGoalRequest request) {
        return goalService.create(currentUserService.getCurrentUser(), request);
    }

    /**
     * Lists goals.
     *
     * @return the user's goals
     */
    @GetMapping
    public GoalListResponse list() {
        return new GoalListResponse(goalService.list(currentUserService.getCurrentUser()));
    }

    /**
     * Gets one goal.
     *
     * @param id goal id
     * @return the goal
     */
    @GetMapping("/{id}")
    public GoalResponse get(@PathVariable Long id) {
        return goalService.get(currentUserService.getCurrentUser(), id);
    }

    /**
     * Updates a goal.
     *
     * @param id      goal id
     * @param request fields to change
     * @return the updated goal
     */
    @PutMapping("/{id}")
    public GoalResponse update(@PathVariable Long id, @Valid @RequestBody UpdateGoalRequest request) {
        return goalService.update(currentUserService.getCurrentUser(), id, request);
    }

    /**
     * Deletes a goal.
     *
     * @param id goal id
     * @return confirmation
     */
    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable Long id) {
        goalService.delete(currentUserService.getCurrentUser(), id);
        return new MessageResponse("Goal deleted successfully");
    }
}
