package com.feather.api.security.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import com.feather.api.service.ResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthenticationHandlerTest {

    private final String apiKey = "api-key";
    private final String accessToken = "access-token";
    private final String refreshToken = "refresh-token";
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private ResponseHandler responseHandler;
    @Mock
    private AuthenticationTokenFactory authenticationTokenFactory;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private User user;
    @Mock
    private FeatherAuthenticationToken providedAuthentication;
    @Mock
    private Authentication currentAuthentication;
    @Mock
    private FeatherCredentials providedCredentials;
    @Mock
    private FeatherCredentials currentCredentials;

    @InjectMocks
    private AuthenticationHandler classUnderTest;

    @Test
    void handleAuthentication_success() throws Exception {
        // Arrange
        when(jwtTokenService.loadUserFromToken(accessToken)).thenReturn(user);
        when(authenticationTokenFactory.buildAuthenticationTokenFromRequest(apiKey, accessToken, refreshToken, user)).thenReturn(providedAuthentication);
        when(authenticationManager.authenticate(providedAuthentication)).thenReturn(currentAuthentication);
        when(providedAuthentication.getCredentials()).thenReturn(providedCredentials);
        when(currentAuthentication.getCredentials()).thenReturn(currentCredentials);

        // Act
        classUnderTest.handleAuthentication(request, response, apiKey, accessToken, refreshToken);

        // Assert
        verify(jwtTokenService).loadUserFromToken(accessToken);
        verify(authenticationTokenFactory).buildAuthenticationTokenFromRequest(apiKey, accessToken, refreshToken, user);
        verify(authenticationManager).authenticate(providedAuthentication);
        verify(responseHandler).updateTokenCookiesIfChanged(response, providedCredentials, currentCredentials);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(currentAuthentication);
    }

    @Test
    void handleAuthentication_jwtAuthenticationException() throws Exception {
        // Arrange
        final JwtAuthenticationException exception = new JwtAuthenticationException("fail");
        when(jwtTokenService.loadUserFromToken(accessToken)).thenThrow(exception);

        // Act
        classUnderTest.handleAuthentication(request, response, apiKey, accessToken, refreshToken);

        // Assert
        verify(responseHandler).handleFailureResponse(request, response, exception);
    }

    @Test
    void handleAuthentication_usernameNotFoundException() throws Exception {
        // Arrange
        final UsernameNotFoundException exception = new UsernameNotFoundException("not found");
        when(jwtTokenService.loadUserFromToken(accessToken)).thenThrow(exception);

        // Act
        classUnderTest.handleAuthentication(request, response, apiKey, accessToken, refreshToken);

        // Assert
        verify(responseHandler).handleFailureResponse(request, response, exception);
    }
}

