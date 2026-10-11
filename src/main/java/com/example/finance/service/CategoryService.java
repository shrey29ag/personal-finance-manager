package com.example.finance.service;

import com.example.finance.dto.request.CategoryRequest;
import com.example.finance.dto.response.CategoryResponse;
import com.example.finance.entity.Category;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ConflictException;
import com.example.finance.exception.ForbiddenException;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.CategoryRepository;
import com.example.finance.repository.TransactionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Category listing, creation, deletion and lookup.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Creates the service.
     *
     * @param categoryRepository    category repository
     * @param transactionRepository transaction repository (for in-use checks)
     */
    public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Lists the default categories plus the user's custom categories.
     *
     * @param user current user
     * @return visible categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> list(User user) {
        return categoryRepository.findVisibleToUser(user.getId()).stream().map(CategoryResponse::from).toList();
    }

    /**
     * Creates a custom category for the user.
     *
     * @param user    current user
     * @param request name and type
     * @return the created category
     * @throws ConflictException if the name matches a default or one of the user's categories
     */
    @Transactional
    public CategoryResponse create(User user, CategoryRequest request) {
        String name = request.name().trim();
        if (findVisible(user, name).isPresent()) {
            throw new ConflictException("Category '" + name + "' already exists");
        }
        return CategoryResponse.from(categoryRepository.save(new Category(name, request.type(), true, user)));
    }

    /**
     * Deletes one of the user's custom categories.
     *
     * @param user current user
     * @param name category name
     * @throws ForbiddenException        if it is a default category
     * @throws ResourceNotFoundException if the user has no such category
     * @throws BadRequestException       if transactions still use it
     */
    @Transactional
    public void delete(User user, String name) {
        if (categoryRepository.findByNameAndUserIsNull(name).isPresent()) {
            throw new ForbiddenException("Default category '" + name + "' cannot be deleted");
        }
        Category category = categoryRepository.findByNameAndUserId(name, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category '" + name + "' not found"));
        if (transactionRepository.existsByCategoryId(category.getId())) {
            throw new BadRequestException("Category '" + name + "' is used by existing transactions and cannot be deleted");
        }
        categoryRepository.delete(category);
    }

    /**
     * Resolves a category name visible to the user, for use by transactions.
     *
     * @param user current user
     * @param name category name
     * @return the category
     * @throws BadRequestException if the user has no access to a category with that name
     */
    @Transactional(readOnly = true)
    public Category resolveForTransaction(User user, String name) {
        return findVisible(user, name == null ? null : name.trim())
                .orElseThrow(() -> new BadRequestException("Category '" + name + "' does not exist"));
    }

    private Optional<Category> findVisible(User user, String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        return categoryRepository.findByNameAndUserIsNull(name)
                .or(() -> categoryRepository.findByNameAndUserId(name, user.getId()));
    }
}
