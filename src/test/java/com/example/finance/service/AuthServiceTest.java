package com.example.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.finance.dto.request.LoginRequest;
import com.example.finance.dto.request.RegisterRequest;
import com.example.finance.dto.response.RegisterResponse;
import com.example.finance.entity.User;
import com.example.finance.exception.ConflictException;
import com.example.finance.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private SecurityContextRepository securityContextRepository;
    @InjectMocks
    private AuthService service;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerHashesPasswordAndReturnsId() {
        when(userRepository.existsByUsername("a@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42L);
            return u;
        });

        RegisterResponse response = service.register(
                new RegisterRequest(" a@example.com ", "secret", "John Doe", "+1234567890"));

        assertThat(response.userId()).isEqualTo(42L);
        assertThat(response.message()).isEqualTo("User registered successfully");
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("a@example.com");
        assertThat(saved.getValue().getPassword()).isEqualTo("hashed");
        assertThat(saved.getValue().getFullName()).isEqualTo("John Doe");
        assertThat(saved.getValue().getPhoneNumber()).isEqualTo("+1234567890");
    }

    @Test
    void registerDuplicateUsernameIsConflict() {
        when(userRepository.existsByUsername("a@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("a@example.com", "secret", "John", "+1234567890")))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginSavesAuthenticatedContextToSession() {
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
                "a@example.com", null, AuthorityUtils.createAuthorityList("ROLE_USER"));
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.login(new LoginRequest("a@example.com", "secret"), request, response);

        ArgumentCaptor<SecurityContext> ctx = ArgumentCaptor.forClass(SecurityContext.class);
        verify(securityContextRepository).saveContext(ctx.capture(), eq(request), eq(response));
        assertThat(ctx.getValue().getAuthentication()).isSameAs(auth);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(auth);
    }

    @Test
    void loginWithBadCredentialsDoesNotSaveContext() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> service.login(new LoginRequest("a@example.com", "wrong"),
                new MockHttpServletRequest(), new MockHttpServletResponse()))
                .isInstanceOf(BadCredentialsException.class);
        verify(securityContextRepository, never()).saveContext(any(), any(), any());
    }

    @Test
    void logoutInvalidatesSessionAndClearsContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("a@example.com", null, AuthorityUtils.NO_AUTHORITIES));

        service.logout(request, new MockHttpServletResponse());

        assertThat(session.isInvalid()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
