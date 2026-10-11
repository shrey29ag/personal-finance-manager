package com.example.finance.repository;

import com.example.finance.entity.CategoryType;
import com.example.finance.entity.Transaction;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Building blocks for filtering transactions. Each method returns {@code null} when its filter value is
 * {@code null}, which {@link Specification#where} and {@code and} treat as "no restriction".
 */
public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    /**
     * Restricts to one user's transactions.
     *
     * @param userId owner id
     * @return the specification
     */
    public static Specification<Transaction> ownedBy(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    /**
     * Restricts to transactions on or after a date.
     *
     * @param startDate inclusive lower bound, or null
     * @return the specification, or null
     */
    public static Specification<Transaction> onOrAfter(LocalDate startDate) {
        return startDate == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), startDate);
    }

    /**
     * Restricts to transactions on or before a date.
     *
     * @param endDate inclusive upper bound, or null
     * @return the specification, or null
     */
    public static Specification<Transaction> onOrBefore(LocalDate endDate) {
        return endDate == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), endDate);
    }

    /**
     * Restricts to a category name.
     *
     * @param name category name, or null
     * @return the specification, or null
     */
    public static Specification<Transaction> categoryName(String name) {
        return name == null ? null : (root, query, cb) -> cb.equal(root.get("category").get("name"), name);
    }

    /**
     * Restricts to a category id.
     *
     * @param categoryId category id, or null
     * @return the specification, or null
     */
    public static Specification<Transaction> categoryId(Long categoryId) {
        return categoryId == null ? null : (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Restricts to a category type.
     *
     * @param type INCOME or EXPENSE, or null
     * @return the specification, or null
     */
    public static Specification<Transaction> ofType(CategoryType type) {
        return type == null ? null : (root, query, cb) -> cb.equal(root.get("category").get("type"), type);
    }
}
