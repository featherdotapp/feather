package com.feather.api.security.filters;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;
import static com.feather.api.shared.TokenType.REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import com.feather.api.security.helpers.AuthenticationHandler;
import com.feather.api.service.CookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

    private final String apiKey = "api-key";
    private final String accessToken = "access-token";
    private final String refreshToken = "refresh-token";
    @Mock
    private CookieService cookieService;
    @Mock
    private AuthenticationHandler authenticationHandler;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private JwtTokenFilter filter;
    private Cookie[] cookies;

    @BeforeEach
    void setUp() {
        cookies = new Cookie[] {
                new Cookie(ACCESS_TOKEN.getCookieName(), accessToken),
                new Cookie(REFRESH_TOKEN.getCookieName(), refreshToken)
        };
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_validTokens_callsAuthenticationHandlerAndFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.findCookie(cookies, REFRESH_TOKEN.getCookieName())).thenReturn(Optional.of(cookies[1]));
        when(cookieService.findCookie(cookies, ACCESS_TOKEN.getCookieName())).thenReturn(Optional.of(cookies[0]));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getCredentials()).thenReturn(apiKey);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(authenticationHandler).handleAuthentication(request, response, apiKey, accessToken, refreshToken);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_missingRefreshTokenCookie_doesNotCallAuthenticationHandler() throws ServletException, IOException {
        // Arrange
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.findCookie(cookies, REFRESH_TOKEN.getCookieName())).thenReturn(Optional.empty());
        when(cookieService.findCookie(cookies, ACCESS_TOKEN.getCookieName())).thenReturn(Optional.of(cookies[0]));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getCredentials()).thenReturn(apiKey);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verifyNoInteractions(authenticationHandler);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_missingAccessTokenCookie_doesNotCallAuthenticationHandler() throws ServletException, IOException {
        // Arrange
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.findCookie(cookies, REFRESH_TOKEN.getCookieName())).thenReturn(Optional.of(cookies[1]));
        when(cookieService.findCookie(cookies, ACCESS_TOKEN.getCookieName())).thenReturn(Optional.empty());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getCredentials()).thenReturn(apiKey);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verifyNoInteractions(authenticationHandler);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_emptyApiKey_doesNotCallAuthenticationHandler() throws ServletException, IOException {
        // Arrange
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.findCookie(cookies, REFRESH_TOKEN.getCookieName())).thenReturn(Optional.of(cookies[1]));
        when(cookieService.findCookie(cookies, ACCESS_TOKEN.getCookieName())).thenReturn(Optional.of(cookies[0]));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getCredentials()).thenReturn("");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verifyNoInteractions(authenticationHandler);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_emptyTokens_doesNotCallAuthenticationHandler() throws ServletException, IOException {
        // Arrange
        final Cookie[] emptyCookies = new Cookie[] {
                new Cookie(ACCESS_TOKEN.getCookieName(), ""),
                new Cookie(REFRESH_TOKEN.getCookieName(), "")
        };
        when(request.getCookies()).thenReturn(emptyCookies);
        when(cookieService.findCookie(emptyCookies, REFRESH_TOKEN.getCookieName())).thenReturn(Optional.of(emptyCookies[1]));
        when(cookieService.findCookie(emptyCookies, ACCESS_TOKEN.getCookieName())).thenReturn(Optional.of(emptyCookies[0]));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getCredentials()).thenReturn(apiKey);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verifyNoInteractions(authenticationHandler);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_emptyAccessToken_doesNotCallAuthenticationHandler() throws ServletException, IOException {
        // Arrange
        final Cookie[] cookiesWithEmptyAccess = new Cookie[] {
                new Cookie(ACCESS_TOKEN.getCookieName(), ""),
                new Cookie(REFRESH_TOKEN.getCookieName(), refreshToken)
        };
        when(request.getCookies()).thenReturn(cookiesWithEmptyAccess);
        when(cookieService.findCookie(cookiesWithEmptyAccess, REFRESH_TOKEN.getCookieName())).thenReturn(Optional.of(cookiesWithEmptyAccess[1]));
        when(cookieService.findCookie(cookiesWithEmptyAccess, ACCESS_TOKEN.getCookieName())).thenReturn(Optional.of(cookiesWithEmptyAccess[0]));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getCredentials()).thenReturn(apiKey);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verifyNoInteractions(authenticationHandler);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_nullCurrentAuth_doesNotCallAuthenticationHandler() throws ServletException, IOException {
        // Arrange
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.findCookie(cookies, REFRESH_TOKEN.getCookieName())).thenReturn(Optional.of(cookies[1]));
        when(cookieService.findCookie(cookies, ACCESS_TOKEN.getCookieName())).thenReturn(Optional.of(cookies[0]));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verifyNoInteractions(authenticationHandler);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_badCredentialsException_loginUrl_doesNotThrow() throws ServletException, IOException {
        // Arrange
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.findCookie(cookies, REFRESH_TOKEN.getCookieName())).thenThrow(new BadCredentialsException("fail"));
        when(request.getRequestURI()).thenReturn("/auth/linkedin/loginUrl");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_badCredentialsException_otherUrl_throws() {
        // Arrange
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.findCookie(cookies, REFRESH_TOKEN.getCookieName())).thenThrow(new BadCredentialsException("fail"));
        when(request.getRequestURI()).thenReturn("/other");

        // Act & Assert
        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Refresh token cookie not found: fail");
    }
}
