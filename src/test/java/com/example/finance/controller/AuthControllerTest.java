package com.example.finance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.finance.dto.request.RegisterRequest;
import com.example.finance.dto.response.RegisterResponse;
import com.example.finance.exception.ConflictException;
import com.example.finance.security.CustomUserDetailsService;
import com.example.finance.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@WithApiSecurity
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private static final String VALID_REGISTER = """
            {"username":"jane@example.com","password":"pw123456","fullName":"Jane Doe","phoneNumber":"+0987654321"}
            """;

    @Test
    void registerReturns201WithoutSession() throws Exception {
        when(authService.register(any())).thenReturn(new RegisterResponse("User registered successfully", 1L));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTER))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"message\":\"User registered successfully\",\"userId\":1}"));
        verify(authService).register(new RegisterRequest("jane@example.com", "pw123456", "Jane Doe", "+0987654321"));
    }

    @Test
    void registerDuplicateIs409() throws Exception {
        when(authService.register(any())).thenThrow(new ConflictException("taken"));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTER))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void registerWithMissingUsernameIs400() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"pw\",\"fullName\":\"X\",\"phoneNumber\":\"+1234567890\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("username: is required"));
        verify(authService, never()).register(any());
    }

    @Test
    void registerWithMissingPasswordIs400() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a@example.com\",\"fullName\":\"X\",\"phoneNumber\":\"+1234567890\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWithInvalidEmailOrPhoneIs400() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"not-an-email\",\"password\":\"pw\",\"fullName\":\"X\",\"phoneNumber\":\"+1234567890\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a@example.com\",\"password\":\"pw\",\"fullName\":\"X\",\"phoneNumber\":\"12ab\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturns200() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a@example.com\",\"password\":\"pw\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Login successful\"}"));
    }

    @Test
    void loginWithBadCredentialsIs401() throws Exception {
        doThrow(new BadCredentialsException("bad")).when(authService).login(any(), any(), any());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginWithMalformedJsonIs400() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void logoutReturns200() throws Exception {
        mvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Logout successful\"}"));
    }

    @Test
    void logoutWithoutSessionIs401() throws Exception {
        mvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }
}
