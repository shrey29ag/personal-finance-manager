package com.example.finance.dto.response;

import com.example.finance.entity.CategoryType;
import com.example.finance.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A transaction as returned by the API.
 *
 * @param id          transaction id
 * @param amount      amount, scale 2
 * @param date        transaction date
 * @param category    category name
 * @param description description
 * @param type        INCOME or EXPENSE, from the category
 */
public record TransactionResponse(Long id, BigDecimal amount, LocalDate date, String category,
                                  String description, CategoryType type) {

    /**
     * Maps an entity to its response.
     *
     * @param t the entity
     * @return the response
     */
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAmount(), t.getDate(), t.getCategory().getName(),
                t.getDescription(), t.getCategory().getType());
    }
}
