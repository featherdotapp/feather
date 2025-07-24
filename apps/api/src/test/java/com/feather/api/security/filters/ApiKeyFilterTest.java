package com.feather.api.security.filters;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.feather.api.security.exception_handling.FeatherAuthenticationEntryPoint;
import com.feather.api.security.exception_handling.exception.ApiKeyAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ApiKeyFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext context;
    @Mock
    private FeatherAuthenticationEntryPoint authenticationEntryPoint;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

    @InjectMocks
    private ApiKeyFilter classUnderTest;

    @BeforeEach
    void setUp() {
        securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMockedStatic.close();
    }

    @Test
    void testDoFilterInternal() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-API-KEY")).thenReturn("someApiKey");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(context);

        // Act
        classUnderTest.doFilterInternal(request, response, filterChain);

        // Assert
        verify(context).setAuthentication(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_providedApiKeyIsNull() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-API-KEY")).thenReturn(null);
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(context);

        // Act
        classUnderTest.doFilterInternal(request, response, filterChain);

        // Assert
        verifyNoInteractions(context);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ServletExceptionIsThrown() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-API-KEY")).thenReturn(null);
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(context);
        doThrow(ServletException.class).when(filterChain).doFilter(request, response);

        // Act & Assert
        assertThrows(ServletException.class, () ->
                classUnderTest.doFilterInternal(request, response, filterChain)
        );
    }

    @Test
    void testDoFilterInternal_IOExceptionIsThrown() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-API-KEY")).thenReturn(null);
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(context);
        doThrow(IOException.class).when(filterChain).doFilter(request, response);

        // Act & Assert
        assertThrows(IOException.class, () ->
                classUnderTest.doFilterInternal(request, response, filterChain)
        );
    }

    @Test
    void testDoFilterInternal_ApiKeyAuthenticationExceptionIsCaught() throws ServletException, IOException {
        // Arrange
        final String apiKey = "invalidApiKey";
        when(request.getHeader("X-API-KEY")).thenReturn(apiKey);
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(context);
        doThrow(new ApiKeyAuthenticationException("Invalid API Key")).when(authenticationManager).authenticate(any());

        // Act
        classUnderTest.doFilterInternal(request, response, filterChain);

        // Assert
        securityContextHolderMockedStatic.verify(SecurityContextHolder::clearContext);
        verify(authenticationEntryPoint).commence(eq(request), eq(response), any(ApiKeyAuthenticationException.class));
        verifyNoInteractions(filterChain);
    }

}