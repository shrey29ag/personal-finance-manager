package com.example.finance.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.finance.TestData;
import com.example.finance.entity.Category;
import com.example.finance.entity.CategoryType;
import com.example.finance.repository.CategoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private DataSeeder seeder;

    @Test
    void seedsAllSevenDefaultsWhenMissing() {
        when(categoryRepository.findByNameAndUserIsNull(anyString())).thenReturn(Optional.empty());

        seeder.run();

        ArgumentCaptor<Category> saved = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(7)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(Category::getName).containsExactly(
                "Salary", "Food", "Rent", "Transportation", "Entertainment", "Healthcare", "Utilities");
        assertThat(saved.getAllValues()).allSatisfy(c -> {
            assertThat(c.isCustom()).isFalse();
            assertThat(c.getUser()).isNull();
        });
        assertThat(saved.getAllValues().get(0).getType()).isEqualTo(CategoryType.INCOME);
    }

    @Test
    void skipsDefaultsThatAlreadyExist() {
        when(categoryRepository.findByNameAndUserIsNull(anyString()))
                .thenReturn(Optional.of(TestData.defaultCategory(1L, "x", CategoryType.EXPENSE)));

        seeder.run();

        verify(categoryRepository, never()).save(any());
    }
}
