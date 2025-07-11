package com.feather.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;

/**
 * Global exception handler for the application.
 * This class provides centralized exception handling for controllers using
 * the @ControllerAdvice annotation. It defines methods to handle specific
 * exceptions and return appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleUserNotFoundException(final AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<String> handleRestClientException(final RestClientException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error communicating with external service: " + e.getMessage());
    }

}
