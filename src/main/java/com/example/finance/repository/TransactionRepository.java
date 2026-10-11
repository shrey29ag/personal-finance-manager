package com.example.finance.repository;

import com.example.finance.entity.CategoryType;
import com.example.finance.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data access for {@link Transaction}. Every query is scoped to a user id; filtered listing goes through
 * {@link JpaSpecificationExecutor} with specifications from {@link TransactionSpecifications}.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /**
     * Finds a transaction owned by the given user.
     *
     * @param id     transaction id
     * @param userId owner id
     * @return the transaction, if it exists and is owned by the user
     */
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /**
     * Checks whether any transaction uses the given category.
     *
     * @param categoryId category id
     * @return true if the category is in use
     */
    boolean existsByCategoryId(Long categoryId);

    /**
     * Lists a user's transactions within an inclusive date range.
     *
     * @param userId owner id
     * @param from   first day
     * @param to     last day
     * @return transactions ordered by date and id
     */
    @Query("""
            select t from Transaction t join fetch t.category
            where t.user.id = :userId and t.date between :from and :to
            order by t.date asc, t.id asc
            """)
    List<Transaction> findInRange(@Param("userId") Long userId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to);

    /**
     * Sums a user's transactions of one type dated on or after {@code since}.
     *
     * @param userId owner id
     * @param type   INCOME or EXPENSE
     * @param since  inclusive start date
     * @return the sum, or null if there are none
     */
    @Query("""
            select sum(t.amount) from Transaction t
            where t.user.id = :userId and t.category.type = :type and t.date >= :since
            """)
    BigDecimal sumByTypeSince(@Param("userId") Long userId,
                              @Param("type") CategoryType type,
                              @Param("since") LocalDate since);
}
