package com.example.finance.service;

import static com.example.finance.repository.TransactionSpecifications.categoryId;
import static com.example.finance.repository.TransactionSpecifications.categoryName;
import static com.example.finance.repository.TransactionSpecifications.ofType;
import static com.example.finance.repository.TransactionSpecifications.onOrAfter;
import static com.example.finance.repository.TransactionSpecifications.onOrBefore;
import static com.example.finance.repository.TransactionSpecifications.ownedBy;

import com.example.finance.dto.request.CreateTransactionRequest;
import com.example.finance.dto.request.UpdateTransactionRequest;
import com.example.finance.dto.response.TransactionResponse;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.Transaction;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.TransactionRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transaction CRUD and filtering. Every operation is scoped to the given user.
 */
@Service
public class TransactionService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("date"), Sort.Order.desc("id"));

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    /**
     * Creates the service.
     *
     * @param transactionRepository transaction repository
     * @param categoryService       category lookup
     */
    public TransactionService(TransactionRepository transactionRepository, CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
    }

    /**
     * Creates a transaction.
     *
     * @param user    current user
     * @param request transaction data
     * @return the created transaction
     */
    @Transactional
    public TransactionResponse create(User user, CreateTransactionRequest request) {
        Transaction transaction = new Transaction(Money.normalize(request.amount()), request.date(),
                request.description(), categoryService.resolveForTransaction(user, request.category()), user);
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    /**
     * Lists the user's transactions, newest first, with optional filters.
     *
     * @param user       current user
     * @param startDate  inclusive lower bound, or null
     * @param endDate    inclusive upper bound, or null
     * @param category   category name, or null
     * @param categoryId category id, or null
     * @param type       INCOME/EXPENSE, or null
     * @return matching transactions
     * @throws BadRequestException if startDate is after endDate
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> list(User user, LocalDate startDate, LocalDate endDate, String category,
                                          Long categoryId, CategoryType type) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must not be after endDate");
        }
        Specification<Transaction> spec = Specification.allOf(ownedBy(user.getId()), onOrAfter(startDate),
                onOrBefore(endDate), categoryName(category), categoryId(categoryId), ofType(type));
        return transactionRepository.findAll(spec, NEWEST_FIRST).stream().map(TransactionResponse::from).toList();
    }

    /**
     * Updates amount, category and/or description. The date is never changed.
     *
     * @param user    current user
     * @param id      transaction id
     * @param request fields to change
     * @return the updated transaction
     * @throws ResourceNotFoundException if the user has no such transaction
     */
    @Transactional
    public TransactionResponse update(User user, Long id, UpdateTransactionRequest request) {
        Transaction transaction = findOwned(user, id);
        if (request.amount() != null) {
            transaction.setAmount(Money.normalize(request.amount()));
        }
        if (request.category() != null) {
            transaction.setCategory(categoryService.resolveForTransaction(user, request.category()));
        }
        if (request.description() != null) {
            transaction.setDescription(request.description());
        }
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    /**
     * Deletes a transaction.
     *
     * @param user current user
     * @param id   transaction id
     * @throws ResourceNotFoundException if the user has no such transaction
     */
    @Transactional
    public void delete(User user, Long id) {
        transactionRepository.delete(findOwned(user, id));
    }

    private Transaction findOwned(User user, Long id) {
        return transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction " + id + " not found"));
    }
}
