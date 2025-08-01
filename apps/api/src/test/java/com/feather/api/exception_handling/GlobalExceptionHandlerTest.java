package com.feather.api.exception_handling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.feather.api.adapter.posthog.service.PostHogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private PostHogService postHogService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler classUnderTest;

    @Test
    void testHandleUserNotFoundException() {
        // Arrange
        final String errorMessage = "Authentication failed";
        final String clientIp = "192.168.1.1";
        final String requestUri = "/api";
        final String distinct = clientIp + "|" + requestUri;
        final AuthenticationException exception = new AuthenticationException(errorMessage) {

        };

        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(request.getRequestURI()).thenReturn(requestUri);

        // Act
        final ResponseEntity<String> response = classUnderTest.handleUserNotFoundException(exception, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(postHogService).trackEvent(eq(distinct), eq("authentication_exception"), any(Map.class));
        verify(request).getRemoteAddr();
    }

    @Test
    void testHandleRestClientException() {
        // Arrange
        final String errorMessage = "Connection timeout";
        final String clientIp = "192.168.1.100";
        final String requestUri = "/api";
        final String distinct = clientIp + "|" + requestUri;
        final RestClientException exception = new RestClientException(errorMessage);

        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(request.getRequestURI()).thenReturn(requestUri);

        // Act
        final ResponseEntity<String> response = classUnderTest.handleRestClientException(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("Error communicating with external service: " + errorMessage, response.getBody());
        verify(postHogService).trackEvent(eq(distinct), eq("rest_client_exception"), any(Map.class));
        verify(request).getRemoteAddr();
    }
}