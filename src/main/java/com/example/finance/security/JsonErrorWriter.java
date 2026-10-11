package com.example.finance.security;

import com.example.finance.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Writes an {@link ErrorResponse} straight to the servlet response. Used by security components that run
 * before the controller layer, where {@code @RestControllerAdvice} does not apply.
 */
@Component
public class JsonErrorWriter {

    private final ObjectMapper objectMapper;

    /**
     * Creates the writer.
     *
     * @param objectMapper Jackson mapper
     */
    public JsonErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Writes the error body and status.
     *
     * @param response servlet response
     * @param status   HTTP status
     * @param message  descriptive message
     * @throws IOException if the body cannot be written
     */
    public void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                new ErrorResponse(status.getReasonPhrase(), message, status.value()));
    }
}
