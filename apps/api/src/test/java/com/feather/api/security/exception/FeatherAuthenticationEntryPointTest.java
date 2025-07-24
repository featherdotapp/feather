package com.feather.api.security.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feather.api.adapter.posthog.service.PostHogService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class FeatherAuthenticationEntryPointTest {

    private static final String REMOTE_IP = "192.168.1.1";
    private static final String ERROR_MESSAGE = "Invalid credentials";

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private PostHogService postHogService;
    @Mock
    private ServletOutputStream outputStream;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private ObjectMapper objectMapper;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

    @InjectMocks
    private FeatherAuthenticationEntryPoint classUnderTest;

    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        authException = new BadCredentialsException(ERROR_MESSAGE);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMockedStatic.close();
    }

    @Test
    void testCommence_setsProperResponseAndCallsPostHogService() throws IOException {
        // Arrange
        when(request.getRemoteAddr()).thenReturn(REMOTE_IP);
        when(response.getOutputStream()).thenReturn(outputStream);

        // Act
        classUnderTest.commence(request, response, authException);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        securityContextHolderMockedStatic.verify(SecurityContextHolder::clearContext);
    }

    @Test
    void testCommence_sendsCorrectDataToPostHog() throws IOException {
        // Arrange
        when(request.getRemoteAddr()).thenReturn(REMOTE_IP);
        when(response.getOutputStream()).thenReturn(outputStream);

        final ArgumentCaptor<Map<String, Object>> postHogPropertiesCaptor = ArgumentCaptor.forClass(Map.class);
        final ArgumentCaptor<String> distinctIdCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> eventNameCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        classUnderTest.commence(request, response, authException);

        // Assert
        verify(postHogService).trackEvent(
                distinctIdCaptor.capture(),
                eventNameCaptor.capture(),
                postHogPropertiesCaptor.capture());
        assertThat(distinctIdCaptor.getValue()).isEqualTo(REMOTE_IP);
        assertThat(eventNameCaptor.getValue()).isEqualTo("authentication_exception");
        final Map<String, Object> properties = postHogPropertiesCaptor.getValue();
        assertThat(properties)
                .containsEntry("error", "Unauthorized")
                .containsEntry("message", ERROR_MESSAGE);
    }
}
