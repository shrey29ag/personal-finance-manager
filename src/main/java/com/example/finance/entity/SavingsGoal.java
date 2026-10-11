package com.example.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A savings goal. Progress is never stored; it is computed from transactions on read.
 */
@Entity
@Table(name = "savings_goals")
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String goalName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @Column(nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    private LocalDate startDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Creates an empty goal (required by JPA). */
    public SavingsGoal() {
    }

    /**
     * Creates a goal.
     *
     * @param goalName     name of the goal
     * @param targetAmount positive target, scale 2
     * @param targetDate   date by which to reach the target
     * @param startDate    date from which transactions count towards progress
     * @param user         owning user
     */
    public SavingsGoal(String goalName, BigDecimal targetAmount, LocalDate targetDate, LocalDate startDate, User user) {
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.startDate = startDate;
        this.user = user;
    }

    /** @return the id */
    public Long getId() {
        return id;
    }

    /** @param id the id */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the goal name */
    public String getGoalName() {
        return goalName;
    }

    /** @return the target amount */
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    /** @param targetAmount the target amount */
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    /** @return the target date */
    public LocalDate getTargetDate() {
        return targetDate;
    }

    /** @param targetDate the target date */
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    /** @return the start date */
    public LocalDate getStartDate() {
        return startDate;
    }

    /** @return the owning user */
    public User getUser() {
        return user;
    }
}
