package com.example.finance.repository;

import com.example.finance.entity.SavingsGoal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link SavingsGoal}. Every query is scoped to a user id.
 */
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    /**
     * Finds a goal owned by the given user.
     *
     * @param id     goal id
     * @param userId owner id
     * @return the goal, if it exists and is owned by the user
     */
    Optional<SavingsGoal> findByIdAndUserId(Long id, Long userId);

    /**
     * Lists a user's goals in creation order.
     *
     * @param userId owner id
     * @return the user's goals
     */
    List<SavingsGoal> findByUserIdOrderByIdAsc(Long userId);
}
