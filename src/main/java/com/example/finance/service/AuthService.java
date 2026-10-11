package com.example.finance.service;

import com.example.finance.dto.request.LoginRequest;
import com.example.finance.dto.request.RegisterRequest;
import com.example.finance.dto.response.RegisterResponse;
import com.example.finance.entity.User;
import com.example.finance.exception.ConflictException;
import com.example.finance.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration, login and logout.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    /**
     * Creates the service.
     *
     * @param userRepository            user repository
     * @param passwordEncoder           password hashing
     * @param authenticationManager     credential checking
     * @param securityContextRepository session-backed context storage
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       SecurityContextRepository securityContextRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * Registers a new user.
     *
     * @param request registration data
     * @return confirmation with the new user's id
     * @throws ConflictException if the username is taken
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username '" + username + "' is already registered");
        }
        User user = userRepository.save(new User(username, passwordEncoder.encode(request.password()),
                request.fullName().trim(), request.phoneNumber().trim()));
        return new RegisterResponse("User registered successfully", user.getId());
    }

    /**
     * Authenticates the user and stores the security context in the HTTP session.
     *
     * @param request     credentials
     * @param httpRequest current request
     * @param httpResponse current response
     * @throws org.springframework.security.core.AuthenticationException on bad credentials
     */
    public void login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username().trim(), request.password()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        // Spring Security 6 no longer saves the context automatically when authenticating manually.
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }

    /**
     * Invalidates the session and clears the security context.
     *
     * @param httpRequest  current request
     * @param httpResponse current response
     */
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        logoutHandler.logout(httpRequest, httpResponse, SecurityContextHolder.getContext().getAuthentication());
    }
}
