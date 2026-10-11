package com.example.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.finance.TestData;
import com.example.finance.dto.request.CreateTransactionRequest;
import com.example.finance.dto.request.UpdateTransactionRequest;
import com.example.finance.dto.response.TransactionResponse;
import com.example.finance.entity.Category;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.Transaction;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private final User user = TestData.user(1L, "a@example.com");
    private final Category salary = TestData.defaultCategory(1L, "Salary", CategoryType.INCOME);
    private final Category food = TestData.defaultCategory(2L, "Food", CategoryType.EXPENSE);

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryService categoryService;
    @InjectMocks
    private TransactionService service;

    @Test
    void createNormalizesAmountAndTakesTypeFromCategory() {
        when(categoryService.resolveForTransaction(user, "Salary")).thenReturn(salary);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        TransactionResponse response = service.create(user, new CreateTransactionRequest(
                new BigDecimal("5000"), LocalDate.of(2024, 1, 15), "Salary", "January Salary"));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.amount()).hasToString("5000.00");
        assertThat(response.category()).isEqualTo("Salary");
        assertThat(response.type()).isEqualTo(CategoryType.INCOME);
        assertThat(response.date()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(response.description()).isEqualTo("January Salary");
    }

    @Test
    void createWithUnknownCategoryPropagatesBadRequest() {
        when(categoryService.resolveForTransaction(user, "Nope")).thenThrow(new BadRequestException("no"));

        assertThatThrownBy(() -> service.create(user, new CreateTransactionRequest(
                BigDecimal.TEN, LocalDate.of(2024, 1, 1), "Nope", null)))
                .isInstanceOf(BadRequestException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listReturnsRepositoryResultsInOrder() {
        when(transactionRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(
                TestData.transaction(2L, "400.00", "2024-01-17", food, user),
                TestData.transaction(1L, "5000.00", "2024-01-15", salary, user)));

        List<TransactionResponse> result = service.list(user, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31),
                null, null, CategoryType.EXPENSE);

        assertThat(result).extracting(TransactionResponse::id).containsExactly(2L, 1L);
    }

    @Test
    void listRejectsStartDateAfterEndDate() {
        assertThatThrownBy(() -> service.list(user, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 1),
                null, null, null)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateChangesAmountCategoryAndDescriptionButNeverDate() {
        Transaction existing = TestData.transaction(5L, "450.00", "2024-01-17", food, user);
        when(transactionRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(existing));
        when(categoryService.resolveForTransaction(user, "Salary")).thenReturn(salary);
        when(transactionRepository.save(existing)).thenReturn(existing);

        TransactionResponse response = service.update(user, 5L,
                new UpdateTransactionRequest(new BigDecimal("5500"), "Salary", "Bonus"));

        assertThat(response.amount()).hasToString("5500.00");
        assertThat(response.category()).isEqualTo("Salary");
        assertThat(response.type()).isEqualTo(CategoryType.INCOME);
        assertThat(response.description()).isEqualTo("Bonus");
        assertThat(response.date()).isEqualTo(LocalDate.of(2024, 1, 17));
    }

    @Test
    void updateWithNoFieldsKeepsEverything() {
        Transaction existing = TestData.transaction(5L, "450.00", "2024-01-17", food, user);
        when(transactionRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(existing)).thenReturn(existing);

        TransactionResponse response = service.update(user, 5L, new UpdateTransactionRequest(null, null, null));

        assertThat(response.amount()).hasToString("450.00");
        assertThat(response.category()).isEqualTo("Food");
        assertThat(response.date()).isEqualTo(LocalDate.of(2024, 1, 17));
    }

    @Test
    void updateOfOtherUsersTransactionIsNotFound() {
        when(transactionRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(user, 5L, new UpdateTransactionRequest(BigDecimal.ONE, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesOwnedTransaction() {
        Transaction existing = TestData.transaction(5L, "450.00", "2024-01-17", food, user);
        when(transactionRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(existing));

        service.delete(user, 5L);

        verify(transactionRepository).delete(existing);
    }

    @Test
    void deleteOfMissingOrOtherUsersTransactionIsNotFound() {
        when(transactionRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(user, 999L)).isInstanceOf(ResourceNotFoundException.class);
        verify(transactionRepository, never()).delete(any(Transaction.class));
    }
}
