package com.feather.api.security.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.feather.api.security.configurations.model.EndpointPaths;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RequestValidatorTest {

    private EndpointPaths endpointPaths;
    private RequestValidator requestValidator;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        endpointPaths = mock(EndpointPaths.class);
        requestValidator = new RequestValidator(endpointPaths);
        request = mock(HttpServletRequest.class);
    }

    @Test
    void matchesNoNeededApiKeyPaths_shouldReturnTrueForUnauthenticatedPath() {
        // Arrange
        final String uri = "/public";
        when(request.getRequestURI()).thenReturn(uri);
        when(endpointPaths.unauthenticatedPaths()).thenReturn(new String[] { "/public", "/other" });
        // Act
        final boolean result = requestValidator.matchesNoNeededApiKeyPaths(request);
        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void matchesNoNeededApiKeyPaths_shouldReturnFalseForNonUnauthenticatedPath() {
        // Arrange
        final String uri = "/secure";
        when(request.getRequestURI()).thenReturn(uri);
        when(endpointPaths.unauthenticatedPaths()).thenReturn(new String[] { "/public", "/other" });
        // Act
        final boolean result = requestValidator.matchesNoNeededApiKeyPaths(request);
        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void matchesNoNeededJwtPaths_shouldReturnTrueForApiKeyAuthenticatedPath() {
        // Arrange
        final String uri = "/api-key";
        when(request.getRequestURI()).thenReturn(uri);
        when(endpointPaths.apiKeyAuthenticatedPaths()).thenReturn(new String[] { "/api-key", "/other-api" });
        when(endpointPaths.unauthenticatedPaths()).thenReturn(new String[] { "/public" });
        // Act
        final boolean result = requestValidator.matchesNoNeededJwtPaths(request);
        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void matchesNoNeededJwtPaths_shouldReturnTrueForUnauthenticatedPath() {
        // Arrange
        final String uri = "/public";
        when(request.getRequestURI()).thenReturn(uri);
        when(endpointPaths.apiKeyAuthenticatedPaths()).thenReturn(new String[] { "/api-key" });
        when(endpointPaths.unauthenticatedPaths()).thenReturn(new String[] { "/public" });
        // Act
        final boolean result = requestValidator.matchesNoNeededJwtPaths(request);
        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void matchesNoNeededJwtPaths_shouldReturnFalseForOtherPath() {
        // Arrange
        final String uri = "/secure";
        when(request.getRequestURI()).thenReturn(uri);
        when(endpointPaths.apiKeyAuthenticatedPaths()).thenReturn(new String[] { "/api-key" });
        when(endpointPaths.unauthenticatedPaths()).thenReturn(new String[] { "/public" });
        // Act
        final boolean result = requestValidator.matchesNoNeededJwtPaths(request);
        // Assert
        assertThat(result).isFalse();
    }
}

