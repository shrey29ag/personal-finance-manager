package com.example.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.finance.TestData;
import com.example.finance.entity.User;
import com.example.finance.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CurrentUserService service;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(username, null, AuthorityUtils.NO_AUTHORITIES));
    }

    @Test
    void returnsUserOfAuthenticatedPrincipal() {
        User user = TestData.user(1L, "a@example.com");
        authenticateAs("a@example.com");
        when(userRepository.findByUsername("a@example.com")).thenReturn(Optional.of(user));

        assertThat(service.getCurrentUser()).isSameAs(user);
    }

    @Test
    void throwsWhenNotAuthenticated() {
        assertThatThrownBy(service::getCurrentUser).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void throwsWhenAuthenticationIsNotAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.unauthenticated("a@example.com", "pw"));

        assertThatThrownBy(service::getCurrentUser).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void throwsWhenUserWasRemoved() {
        authenticateAs("gone@example.com");
        when(userRepository.findByUsername("gone@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(service::getCurrentUser).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }
}
