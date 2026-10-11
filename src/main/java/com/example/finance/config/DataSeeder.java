package com.example.finance.config;

import com.example.finance.entity.Category;
import com.example.finance.entity.CategoryType;
import com.example.finance.repository.CategoryRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the global default categories on startup.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Map<String, CategoryType> DEFAULTS = new LinkedHashMap<>();

    static {
        DEFAULTS.put("Salary", CategoryType.INCOME);
        DEFAULTS.put("Food", CategoryType.EXPENSE);
        DEFAULTS.put("Rent", CategoryType.EXPENSE);
        DEFAULTS.put("Transportation", CategoryType.EXPENSE);
        DEFAULTS.put("Entertainment", CategoryType.EXPENSE);
        DEFAULTS.put("Healthcare", CategoryType.EXPENSE);
        DEFAULTS.put("Utilities", CategoryType.EXPENSE);
    }

    private final CategoryRepository categoryRepository;

    /**
     * Creates the seeder.
     *
     * @param categoryRepository category repository
     */
    public DataSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Inserts any default category that does not exist yet.
     *
     * @param args ignored
     */
    @Override
    public void run(String... args) {
        DEFAULTS.forEach((name, type) -> {
            if (categoryRepository.findByNameAndUserIsNull(name).isEmpty()) {
                categoryRepository.save(new Category(name, type, false, null));
            }
        });
    }
}
