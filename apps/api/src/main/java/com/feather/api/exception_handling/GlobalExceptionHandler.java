package com.feather.api.exception_handling;

import java.util.HashMap;
import java.util.Map;

import com.feather.api.adapter.posthog.service.PostHogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final PostHogService postHogService;

    /**
     * Handles authentication exceptions.
     * Tracks the event and returns an HTTP 401 Unauthorized response.
     *
     * @param e the authentication exception
     * @param request the HTTP servlet request
     * @return a response entity with the error message and HTTP status
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleUserNotFoundException(final AuthenticationException e, final HttpServletRequest request) {
        final String clientIp = request.getRemoteAddr();
        final Map<String, Object> properties = new HashMap<>();
        postHogService.trackEvent(clientIp, "authentication_exception", properties);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    /**
     * Handles REST client exceptions.
     * Tracks the event and returns an HTTP 502 Bad Gateway response.
     *
     * @param e the REST client exception
     * @param request the HTTP servlet request
     * @return a response entity with the error message and HTTP status
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<String> handleRestClientException(final RestClientException e, final HttpServletRequest request) {
        final Map<String, Object> properties = new HashMap<>();
        postHogService.trackEvent(request.getRemoteAddr(), "rest_client_exception", properties);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error communicating with external service: " + e.getMessage());
    }

}
