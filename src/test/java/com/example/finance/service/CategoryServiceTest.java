package com.example.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.finance.TestData;
import com.example.finance.dto.request.CategoryRequest;
import com.example.finance.dto.response.CategoryResponse;
import com.example.finance.entity.Category;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ConflictException;
import com.example.finance.exception.ForbiddenException;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.CategoryRepository;
import com.example.finance.repository.TransactionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    private final User user = TestData.user(1L, "a@example.com");

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private CategoryService service;

    @Test
    void listMapsDefaultAndCustomCategories() {
        when(categoryRepository.findVisibleToUser(1L)).thenReturn(List.of(
                TestData.defaultCategory(1L, "Salary", CategoryType.INCOME),
                TestData.customCategory(9L, "Freelance", CategoryType.INCOME, user)));

        List<CategoryResponse> result = service.list(user);

        assertThat(result).extracting(CategoryResponse::getName).containsExactly("Salary", "Freelance");
        assertThat(result).extracting(CategoryResponse::isCustom).containsExactly(false, true);
        assertThat(result.get(1).getIsCustom()).isTrue();
        assertThat(result.get(1).getType()).isEqualTo(CategoryType.INCOME);
    }

    @Test
    void createSavesTrimmedCustomCategory() {
        when(categoryRepository.findByNameAndUserIsNull("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserId("Freelance", 1L)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryResponse response = service.create(user, new CategoryRequest("  Freelance ", CategoryType.INCOME));

        assertThat(response.getName()).isEqualTo("Freelance");
        assertThat(response.isCustom()).isTrue();
    }

    @Test
    void createRejectsNameOfDefaultCategoryWith409() {
        when(categoryRepository.findByNameAndUserIsNull("Food"))
                .thenReturn(Optional.of(TestData.defaultCategory(2L, "Food", CategoryType.EXPENSE)));

        assertThatThrownBy(() -> service.create(user, new CategoryRequest("Food", CategoryType.EXPENSE)))
                .isInstanceOf(ConflictException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateOwnCategoryWith409() {
        when(categoryRepository.findByNameAndUserIsNull("Gym")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserId("Gym", 1L))
                .thenReturn(Optional.of(TestData.customCategory(9L, "Gym", CategoryType.EXPENSE, user)));

        assertThatThrownBy(() -> service.create(user, new CategoryRequest("Gym", CategoryType.EXPENSE)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteDefaultCategoryIsForbidden() {
        when(categoryRepository.findByNameAndUserIsNull("Food"))
                .thenReturn(Optional.of(TestData.defaultCategory(2L, "Food", CategoryType.EXPENSE)));

        assertThatThrownBy(() -> service.delete(user, "Food")).isInstanceOf(ForbiddenException.class);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteUnknownCategoryIsNotFound() {
        when(categoryRepository.findByNameAndUserIsNull("Nope")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserId("Nope", 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(user, "Nope")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteCategoryInUseIsBadRequest() {
        Category gym = TestData.customCategory(9L, "Gym", CategoryType.EXPENSE, user);
        when(categoryRepository.findByNameAndUserIsNull("Gym")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserId("Gym", 1L)).thenReturn(Optional.of(gym));
        when(transactionRepository.existsByCategoryId(9L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(user, "Gym")).isInstanceOf(BadRequestException.class);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteUnusedCustomCategory() {
        Category gym = TestData.customCategory(9L, "Gym", CategoryType.EXPENSE, user);
        when(categoryRepository.findByNameAndUserIsNull("Gym")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserId("Gym", 1L)).thenReturn(Optional.of(gym));
        when(transactionRepository.existsByCategoryId(9L)).thenReturn(false);

        service.delete(user, "Gym");

        verify(categoryRepository).delete(gym);
    }

    @Test
    void resolveForTransactionFindsDefaultThenCustom() {
        Category gym = TestData.customCategory(9L, "Gym", CategoryType.EXPENSE, user);
        when(categoryRepository.findByNameAndUserIsNull("Gym")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserId("Gym", 1L)).thenReturn(Optional.of(gym));

        assertThat(service.resolveForTransaction(user, " Gym ")).isSameAs(gym);
    }

    @Test
    void resolveForTransactionRejectsUnknownBlankAndNullNames() {
        when(categoryRepository.findByNameAndUserIsNull("Unknown")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserId("Unknown", 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveForTransaction(user, "Unknown")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.resolveForTransaction(user, "  ")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.resolveForTransaction(user, null)).isInstanceOf(BadRequestException.class);
    }
}
