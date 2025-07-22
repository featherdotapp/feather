package com.feather.api.security.exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feather.api.adapter.posthog.service.PostHogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Custom AuthenticationEntryPoint for handling unauthorized access attempts in the application.
 * <p>
 * This entry point is triggered whenever an unauthenticated user tries to access a protected resource.
 * It responds with a JSON object containing error details, including the error type and message.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class FeatherAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final PostHogService postHogService;

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        final Map<String, Object> errorDetails = handleError(request, response, authException);
        SecurityContextHolder.clearContext();
        new ObjectMapper().writeValue(response.getOutputStream(), errorDetails);
    }

    private Map<String, Object> handleError(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) {
        final Map<String, Object> errorDetails = new HashMap<>();
        if (Boolean.TRUE.equals(request.getAttribute("rateLimitExceeded"))) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            errorDetails.put("error", "Too Many Requests");
            errorDetails.put("message", "Rate limit exceeded");
            postHogService.trackEvent(request.getRemoteAddr(), "rate_limit_exceeded", errorDetails);
        } else {
            errorDetails.put("error", "Unauthorized");
            errorDetails.put("message", authException.getMessage());
            postHogService.trackEvent(request.getRemoteAddr(), "authentication_exception", errorDetails);
        }
        return errorDetails;
    }
}
