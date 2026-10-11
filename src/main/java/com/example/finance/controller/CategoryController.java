package com.example.finance.controller;

import com.example.finance.dto.request.CategoryRequest;
import com.example.finance.dto.response.CategoryListResponse;
import com.example.finance.dto.response.CategoryResponse;
import com.example.finance.dto.response.MessageResponse;
import com.example.finance.service.CategoryService;
import com.example.finance.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Category endpoints.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;

    /**
     * Creates the controller.
     *
     * @param categoryService    category service
     * @param currentUserService current-user lookup
     */
    public CategoryController(CategoryService categoryService, CurrentUserService currentUserService) {
        this.categoryService = categoryService;
        this.currentUserService = currentUserService;
    }

    /**
     * Lists default and custom categories.
     *
     * @return the categories
     */
    @GetMapping
    public CategoryListResponse list() {
        return new CategoryListResponse(categoryService.list(currentUserService.getCurrentUser()));
    }

    /**
     * Creates a custom category.
     *
     * @param request name and type
     * @return the created category
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(currentUserService.getCurrentUser(), request);
    }

    /**
     * Deletes a custom category.
     *
     * @param name category name
     * @return confirmation
     */
    @DeleteMapping("/{name}")
    public MessageResponse delete(@PathVariable String name) {
        categoryService.delete(currentUserService.getCurrentUser(), name);
        return new MessageResponse("Category deleted successfully");
    }
}
