package com.feather.api.security.helpers;

import java.util.Arrays;

import com.feather.api.security.configurations.model.EndpointPaths;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates incoming HTTP requests against configured endpoint paths
 */
@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final EndpointPaths endpointPaths;

    /**
     * Checks if the request URI matches any unauthenticated endpoint path.
     *
     * @param request the incoming HTTP request
     * @return true if the request URI is in unauthenticatedPaths, false otherwise
     */
    public boolean matchesNoNeededApiKeyPaths(final HttpServletRequest request) {
        final String requestURI = request.getRequestURI();
        return Arrays.asList(endpointPaths.unauthenticatedPaths()).contains(requestURI);
    }

    /**
     * Checks if the request URI matches any API key authenticated or unauthenticated endpoint path.
     *
     * @param request the incoming HTTP request
     * @return true if the request URI is in apiKeyAuthenticatedPaths or unauthenticatedPaths, false otherwise
     */
    public boolean matchesNoNeededJwtPaths(final HttpServletRequest request) {
        final String requestURI = request.getRequestURI();
        return Arrays.asList(endpointPaths.apiKeyAuthenticatedPaths()).contains(requestURI) ||
                Arrays.asList(endpointPaths.unauthenticatedPaths()).contains(requestURI);
    }

}
