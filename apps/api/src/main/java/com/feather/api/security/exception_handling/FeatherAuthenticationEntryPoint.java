package com.feather.api.security.exception_handling;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feather.api.adapter.posthog.service.PostHogService;
import com.feather.api.security.exception_handling.exception.ApiKeyAuthenticationException;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        final Map<String, Object> errorDetails = Map.of(
                "error", "Unauthorized",
                "message", authException.getMessage()
        );
        handlePostHogEvent(request, authException, errorDetails);
        SecurityContextHolder.clearContext();
        new ObjectMapper().writeValue(response.getOutputStream(), errorDetails);
    }

    private void handlePostHogEvent(final HttpServletRequest request, final AuthenticationException authException, final Map<String, Object> errorDetails) {
        final String event = switch (authException) {
            case final JwtAuthenticationException ignored -> "jwt_authentication_exception";
            case final UsernameNotFoundException ignored -> "user_not_found_exception";
            case final ApiKeyAuthenticationException ignored -> "api_key_authentication_exception";
            default -> "authentication_exception";
        };
        postHogService.trackEvent(request.getRemoteAddr(), event, errorDetails);
    }
}
