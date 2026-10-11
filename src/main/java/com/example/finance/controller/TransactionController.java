package com.example.finance.controller;

import com.example.finance.dto.request.CreateTransactionRequest;
import com.example.finance.dto.request.UpdateTransactionRequest;
import com.example.finance.dto.response.MessageResponse;
import com.example.finance.dto.response.TransactionListResponse;
import com.example.finance.dto.response.TransactionResponse;
import com.example.finance.entity.CategoryType;
import com.example.finance.service.CurrentUserService;
import com.example.finance.service.TransactionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transaction endpoints.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CurrentUserService currentUserService;

    /**
     * Creates the controller.
     *
     * @param transactionService transaction service
     * @param currentUserService current-user lookup
     */
    public TransactionController(TransactionService transactionService, CurrentUserService currentUserService) {
        this.transactionService = transactionService;
        this.currentUserService = currentUserService;
    }

    /**
     * Creates a transaction.
     *
     * @param request transaction data
     * @return the created transaction
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody CreateTransactionRequest request) {
        return transactionService.create(currentUserService.getCurrentUser(), request);
    }

    /**
     * Lists transactions with optional filters.
     *
     * @param startDate  inclusive lower bound
     * @param endDate    inclusive upper bound
     * @param category   category name
     * @param categoryId category id
     * @param type       INCOME or EXPENSE
     * @return matching transactions, newest first
     */
    @GetMapping
    public TransactionListResponse list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CategoryType type) {
        return new TransactionListResponse(transactionService.list(currentUserService.getCurrentUser(),
                startDate, endDate, category, categoryId, type));
    }

    /**
     * Updates a transaction (the date is ignored).
     *
     * @param id      transaction id
     * @param request fields to change
     * @return the updated transaction
     */
    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTransactionRequest request) {
        return transactionService.update(currentUserService.getCurrentUser(), id, request);
    }

    /**
     * Deletes a transaction.
     *
     * @param id transaction id
     * @return confirmation
     */
    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable Long id) {
        transactionService.delete(currentUserService.getCurrentUser(), id);
        return new MessageResponse("Transaction deleted successfully");
    }
}
