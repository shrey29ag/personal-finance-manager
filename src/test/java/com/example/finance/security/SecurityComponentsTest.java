package com.example.finance.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.finance.TestData;
import com.example.finance.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class SecurityComponentsTest {

    private final JsonErrorWriter writer = new JsonErrorWriter(new ObjectMapper());

    @Test
    void entryPointWrites401Json() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JsonAuthenticationEntryPoint(writer).commence(new MockHttpServletRequest(), response,
                new InsufficientAuthenticationException("no session"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString())
                .isEqualTo("{\"error\":\"Unauthorized\",\"message\":\"Authentication required. Please log in.\",\"status\":401}");
    }

    @Test
    void accessDeniedHandlerWrites403Json() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JsonAccessDeniedHandler(writer).handle(new MockHttpServletRequest(), response,
                new AccessDeniedException("nope"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("\"status\":403");
    }

    @Test
    void userDetailsServiceLoadsUserByUsername() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findByUsername("a@example.com")).thenReturn(Optional.of(TestData.user(1L, "a@example.com")));

        UserDetails details = new CustomUserDetailsService(repo).loadUserByUsername("a@example.com");

        assertThat(details.getUsername()).isEqualTo("a@example.com");
        assertThat(details.getPassword()).isEqualTo("hash");
        assertThat(details.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_USER");
    }

    @Test
    void userDetailsServiceThrowsForUnknownUser() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findByUsername("x@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CustomUserDetailsService(repo).loadUserByUsername("x@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
