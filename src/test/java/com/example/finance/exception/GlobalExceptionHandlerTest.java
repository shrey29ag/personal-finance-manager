package com.example.finance.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.finance.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

/**
 * Handlers that the controller tests cannot reach through a real endpoint.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void constraintViolationIs400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleConstraintViolation(new ConstraintViolationException("bad", Set.of()));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    @Test
    void missingParameterIs400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleMissingParam(new MissingServletRequestParameterException("year", "int"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("year");
    }

    @Test
    void unexpectedExceptionIs500WithoutLeakingDetails() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(new IllegalStateException("db password=x"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).doesNotContain("password");
    }
}
