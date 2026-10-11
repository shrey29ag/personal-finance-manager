package com.example.finance.controller;

import com.example.finance.dto.request.LoginRequest;
import com.example.finance.dto.request.RegisterRequest;
import com.example.finance.dto.response.MessageResponse;
import com.example.finance.dto.response.RegisterResponse;
import com.example.finance.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registration, login and logout endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Creates the controller.
     *
     * @param authService auth service
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     *
     * @param request registration data
     * @return confirmation with the user id
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Logs in and starts a session (JSESSIONID cookie).
     *
     * @param request      credentials
     * @param httpRequest  current request
     * @param httpResponse current response
     * @return confirmation
     */
    @PostMapping("/login")
    public MessageResponse login(@Valid @RequestBody LoginRequest request,
                                 HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.login(request, httpRequest, httpResponse);
        return new MessageResponse("Login successful");
    }

    /**
     * Logs out and invalidates the session.
     *
     * @param httpRequest  current request
     * @param httpResponse current response
     * @return confirmation
     */
    @PostMapping("/logout")
    public MessageResponse logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.logout(httpRequest, httpResponse);
        return new MessageResponse("Logout successful");
    }
}
