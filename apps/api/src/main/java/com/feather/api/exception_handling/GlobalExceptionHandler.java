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
        final String event = "authentication_exception";
        final Map<String, Object> properties = new HashMap<>();
        return trackAndBuildErrorResponse(clientIp, event, properties, HttpStatus.UNAUTHORIZED, e.getMessage());
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
        final String clientIp = request.getRemoteAddr();
        final String event = "rest_client_exception";
        final String errorMessage = "Error communicating with external service: " + e.getMessage();
        final Map<String, Object> properties = new HashMap<>();
        return trackAndBuildErrorResponse(clientIp, event, properties, HttpStatus.BAD_GATEWAY, errorMessage);
    }

    private ResponseEntity<String> trackAndBuildErrorResponse(final String eventDistinct, final String event, final Map<String, Object> properties,
            final HttpStatus status, final String errorMessage) {
        postHogService.trackEvent(eventDistinct, event, properties);
        return ResponseEntity.status(status).body(errorMessage);
    }

}
