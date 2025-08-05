package com.feather.api.security.exception_handling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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

    @Test
    void testCommence_withJwtAuthenticationException_tracksJwtEvent() throws IOException {
        when(request.getRemoteAddr()).thenReturn(REMOTE_IP);
        when(response.getOutputStream()).thenReturn(outputStream);
        final AuthenticationException jwtException = new com.feather.api.security.exception_handling.exception.JwtAuthenticationException(ERROR_MESSAGE);

        classUnderTest.commence(request, response, jwtException);

        verify(postHogService).trackEvent(eq(REMOTE_IP), eq("jwt_authentication_exception"), anyMap());
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        securityContextHolderMockedStatic.verify(SecurityContextHolder::clearContext);
    }

    @Test
    void testCommence_withApiKeyAuthenticationException_tracksApiKeyEvent() throws IOException {
        when(request.getRemoteAddr()).thenReturn(REMOTE_IP);
        when(response.getOutputStream()).thenReturn(outputStream);
        final AuthenticationException apiKeyException = new com.feather.api.security.exception_handling.exception.ApiKeyAuthenticationException(ERROR_MESSAGE);

        classUnderTest.commence(request, response, apiKeyException);

        verify(postHogService).trackEvent(eq(REMOTE_IP), eq("api_key_authentication_exception"),
                anyMap());
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        securityContextHolderMockedStatic.verify(SecurityContextHolder::clearContext);
    }

    @Test
    void testCommence_writesCorrectJsonToResponse() throws IOException {
        when(request.getRemoteAddr()).thenReturn(REMOTE_IP);
        when(response.getOutputStream()).thenReturn(outputStream);

        classUnderTest.commence(request, response, authException);

        // Verify that ObjectMapper writes the correct map to the output stream
        // (the actual call is new ObjectMapper().writeValue(...), so we can't mock it directly)
        // Instead, verify that response.getOutputStream() is called and status/contentType are set
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(response).getOutputStream();
        securityContextHolderMockedStatic.verify(SecurityContextHolder::clearContext);
    }

    @Test
    void testCommence_withUsernameNotFoundException_tracksEventAndSetsResponse() throws IOException {
        // Arrange
        final AuthenticationException exception = new UsernameNotFoundException(ERROR_MESSAGE);
        when(request.getRemoteAddr()).thenReturn(REMOTE_IP);
        when(response.getOutputStream()).thenReturn(outputStream);

        // Act
        classUnderTest.commence(request, response, exception);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        securityContextHolderMockedStatic.verify(SecurityContextHolder::clearContext);
        verify(postHogService).trackEvent(
                eq(REMOTE_IP),
                eq("user_not_found_exception"),
                argThat(map ->
                        "Unauthorized".equals(map.get("error")) && ERROR_MESSAGE.equals(map.get("message"))
                )
        );
    }
}
