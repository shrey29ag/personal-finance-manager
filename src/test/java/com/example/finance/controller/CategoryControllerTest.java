package com.example.finance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.finance.TestData;
import com.example.finance.dto.response.CategoryResponse;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ConflictException;
import com.example.finance.exception.ForbiddenException;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.security.CustomUserDetailsService;
import com.example.finance.service.CategoryService;
import com.example.finance.service.CurrentUserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
@WithApiSecurity
@WithMockUser
class CategoryControllerTest {

    private final User user = TestData.user(1L, "a@example.com");

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private CurrentUserService currentUserService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void listEmitsBothCustomAndIsCustom() throws Exception {
        when(categoryService.list(user)).thenReturn(List.of(
                new CategoryResponse("Salary", CategoryType.INCOME, false),
                new CategoryResponse("Freelance", CategoryType.INCOME, true)));

        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"categories\":["
                        + "{\"name\":\"Salary\",\"type\":\"INCOME\",\"custom\":false,\"isCustom\":false},"
                        + "{\"name\":\"Freelance\",\"type\":\"INCOME\",\"custom\":true,\"isCustom\":true}]}"));
    }

    @Test
    @WithAnonymousUser
    void listWithoutSessionIs401() throws Exception {
        mvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void createReturns201() throws Exception {
        when(categoryService.create(eq(user), any()))
                .thenReturn(new CategoryResponse("Freelance_1", CategoryType.INCOME, true));

        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Freelance_1\",\"type\":\"INCOME\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.custom").value(true))
                .andExpect(jsonPath("$.isCustom").value(true));
    }

    @Test
    void createWithInvalidTypeIs400() throws Exception {
        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"InvalidType\",\"type\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());
        verify(categoryService, never()).create(any(), any());
    }

    @Test
    void createWithBlankNameIs400() throws Exception {
        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"type\":\"INCOME\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDuplicateIs409() throws Exception {
        when(categoryService.create(eq(user), any())).thenThrow(new ConflictException("exists"));

        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Food\",\"type\":\"EXPENSE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteReturnsMessage() throws Exception {
        mvc.perform(delete("/api/categories/Gym"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Category deleted successfully\"}"));
        verify(categoryService).delete(user, "Gym");
    }

    @Test
    void deleteDefaultCategoryIs403() throws Exception {
        doThrow(new ForbiddenException("default")).when(categoryService).delete(user, "Food");

        mvc.perform(delete("/api/categories/Food"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void deleteCategoryInUseIs400() throws Exception {
        doThrow(new BadRequestException("in use")).when(categoryService).delete(user, "Freelance");

        mvc.perform(delete("/api/categories/Freelance")).andExpect(status().isBadRequest());
    }

    @Test
    void deleteUnknownCategoryIs404() throws Exception {
        doThrow(new ResourceNotFoundException("missing")).when(categoryService).delete(user, "Nope");

        mvc.perform(delete("/api/categories/Nope")).andExpect(status().isNotFound());
    }
}
