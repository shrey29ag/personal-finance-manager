package com.example.finance.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.finance.TestData;
import com.example.finance.dto.request.UpdateTransactionRequest;
import com.example.finance.dto.response.TransactionResponse;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.security.CustomUserDetailsService;
import com.example.finance.service.CurrentUserService;
import com.example.finance.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
@WithApiSecurity
@WithMockUser
class TransactionControllerTest {

    private final User user = TestData.user(1L, "a@example.com");
    private final TransactionResponse salary = new TransactionResponse(1L, new BigDecimal("5000.00"),
            LocalDate.of(2024, 1, 15), "Salary", "January Salary", CategoryType.INCOME);

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private TransactionService transactionService;
    @MockitoBean
    private CurrentUserService currentUserService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentUser()).thenReturn(user);
    }

    private static String body(String amount, String date) {
        return "{\"amount\":" + amount + ",\"date\":\"" + date + "\",\"category\":\"Food\",\"description\":\"x\"}";
    }

    @Test
    void createReturns201WithExactJson() throws Exception {
        when(transactionService.create(eq(user), any())).thenReturn(salary);

        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content(body("5000.00", "2024-01-15")))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"id\":1,\"amount\":5000.00,\"date\":\"2024-01-15\","
                        + "\"category\":\"Salary\",\"description\":\"January Salary\",\"type\":\"INCOME\"}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-100.00", "0.00"})
    void createWithZeroOrNegativeAmountIs400(String amount) throws Exception {
        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content(body(amount, "2024-01-17")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("amount: must be greater than 0"));
        verify(transactionService, never()).create(any(), any());
    }

    @Test
    void createWithFutureDateIs400() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).toString();

        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content(body("100.00", tomorrow)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("date: cannot be in the future"));
    }

    @Test
    void createWithTodaysDateIsAccepted() throws Exception {
        when(transactionService.create(eq(user), any())).thenReturn(salary);

        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content(body("100.00", LocalDate.now().toString())))
                .andExpect(status().isCreated());
    }

    @Test
    void createWithInvalidDateFormatIs400() throws Exception {
        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content(body("100.00", "01-17-2024")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithMissingFieldsIs400() throws Exception {
        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithUnknownCategoryIs400() throws Exception {
        when(transactionService.create(eq(user), any())).thenThrow(new BadRequestException("no category"));

        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content(body("100.00", "2024-01-18")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithTextBodyIs415() throws Exception {
        mvc.perform(post("/api/transactions").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void listPassesFiltersAndWrapsResult() throws Exception {
        when(transactionService.list(user, LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 16), "Salary", 3L,
                CategoryType.INCOME)).thenReturn(List.of(salary));

        mvc.perform(get("/api/transactions")
                        .param("startDate", "2024-01-15").param("endDate", "2024-01-16")
                        .param("category", "Salary").param("categoryId", "3").param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.transactions[0].id").value(1));
    }

    @Test
    void emptyListSerializesAsEmptyArray() throws Exception {
        when(transactionService.list(user, null, null, null, null, null)).thenReturn(List.of());

        mvc.perform(get("/api/transactions"))
                .andExpect(content().string("{\"transactions\":[]}"));
    }

    @Test
    void listWithInvalidTypeOrDateIs400() throws Exception {
        mvc.perform(get("/api/transactions").param("type", "BOGUS")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/transactions").param("startDate", "15-01-2024")).andExpect(status().isBadRequest());
    }

    @Test
    void updateIgnoresDateField() throws Exception {
        TransactionResponse updated = new TransactionResponse(3L, new BigDecimal("450.00"),
                LocalDate.of(2024, 1, 17), "Food", "Trying to change date", CategoryType.EXPENSE);
        UpdateTransactionRequest expected =
                new UpdateTransactionRequest(new BigDecimal("450.00"), null, "Trying to change date");
        when(transactionService.update(user, 3L, expected)).thenReturn(updated);

        mvc.perform(put("/api/transactions/3").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":450.00,\"date\":\"2024-01-18\",\"description\":\"Trying to change date\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2024-01-17"))
                .andExpect(content().string(containsString("\"amount\":450.00")));
        verify(transactionService).update(user, 3L, expected);
    }

    @Test
    void updateWithZeroAmountIs400() throws Exception {
        mvc.perform(put("/api/transactions/3").contentType(MediaType.APPLICATION_JSON).content("{\"amount\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateOtherUsersTransactionIs404() throws Exception {
        when(transactionService.update(eq(user), eq(999L), any())).thenThrow(new ResourceNotFoundException("nope"));

        mvc.perform(put("/api/transactions/999").contentType(MediaType.APPLICATION_JSON).content("{\"amount\":1000}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateWithNonNumericIdIs400() throws Exception {
        mvc.perform(put("/api/transactions/abc").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReturnsMessage() throws Exception {
        mvc.perform(delete("/api/transactions/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Transaction deleted successfully\"}"));
        verify(transactionService).delete(user, 2L);
    }

    @Test
    void deleteMissingTransactionIs404() throws Exception {
        doThrow(new ResourceNotFoundException("nope")).when(transactionService).delete(user, 999L);

        mvc.perform(delete("/api/transactions/999")).andExpect(status().isNotFound());
    }

    @Test
    void unsupportedMethodIs405() throws Exception {
        mvc.perform(patch("/api/transactions/1")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void unknownPathIs404() throws Exception {
        mvc.perform(get("/api/does-not-exist")).andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void withoutSessionIs401() throws Exception {
        mvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
    }
}
