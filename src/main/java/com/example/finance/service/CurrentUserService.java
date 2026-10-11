package com.example.finance.service;

import com.example.finance.entity.User;
import com.example.finance.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resolves the {@link User} behind the current authenticated session.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    /**
     * Creates the service.
     *
     * @param userRepository user repository
     */
    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the logged-in user.
     *
     * @return the current user
     * @throws AuthenticationCredentialsNotFoundException if nobody is logged in
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Not authenticated");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("User no longer exists"));
    }
}
