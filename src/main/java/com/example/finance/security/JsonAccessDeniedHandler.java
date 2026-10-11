package com.example.finance.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Answers authorization failures with a 403 JSON body.
 */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final JsonErrorWriter errorWriter;

    /**
     * Creates the handler.
     *
     * @param errorWriter JSON error writer
     */
    public JsonAccessDeniedHandler(JsonErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    /**
     * Writes a 403 response.
     *
     * @param request               the request
     * @param response              the response
     * @param accessDeniedException the cause
     * @throws IOException if the body cannot be written
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        errorWriter.write(response, HttpStatus.FORBIDDEN, "Access denied");
    }
}
