package com.example.finance.dto.response;

/**
 * Uniform error body returned for every failed request.
 *
 * @param error   short error label, e.g. "Bad Request"
 * @param message descriptive message
 * @param status  HTTP status code
 */
public record ErrorResponse(String error, String message, int status) {
}
