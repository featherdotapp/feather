package com.feather.api.security.filters;

import static com.feather.api.shared.AuthenticationConstants.AUTHORIZATION_HEADER;
import static com.feather.api.shared.AuthenticationConstants.BEARER_PREFIX;
import static com.feather.api.shared.AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.jpa.service.UserService;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.service.CookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

    private static final String VALID_TOKEN = "valid-jwt-token";
    private static final String VALID_REFRESH_TOKEN = "valid-refresh-token";
    private static final String VALID_API_KEY = "valid-api-key";
    private static final String TEST_USERNAME = "test@example.com";

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
    private Authentication resultAuthentication;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private CookieService cookieService;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private UserService userService;
    @Mock
    private User user;
    @Mock
    private Cookie refreshTokenCookie;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

    @InjectMocks
    private JwtTokenFilter classUnderTest;

    @BeforeEach
    void setUp() {
        securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMockedStatic.close();
    }

    @Test
    void testDoFilterInternal_withValidTokens_authenticatesSuccessfully() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME))).thenReturn(Optional.of(refreshTokenCookie));
        when(refreshTokenCookie.getValue()).thenReturn(VALID_REFRESH_TOKEN);

        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getCredentials()).thenReturn(VALID_API_KEY);

        when(jwtTokenService.extractUsername(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(userService.getUserFromEmail(TEST_USERNAME)).thenReturn(user);
        when(authenticationManager.authenticate(any(FeatherAuthenticationToken.class))).thenReturn(resultAuthentication);

        // Act
        classUnderTest.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext).setAuthentication(resultAuthentication);
        verify(filterChain).doFilter(request, response);

        // Verify the token extraction and user loading
        verify(jwtTokenService).extractUsername(VALID_TOKEN);
        verify(userService).getUserFromEmail(TEST_USERNAME);
    }

    @Test
    void testDoFilterInternal_withMissingAuthorizationHeader_skipsAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        // Act
        classUnderTest.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void testDoFilterInternal_withMissingRefreshTokenCookie_skipsAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME))).thenReturn(Optional.empty());
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        // Act
        classUnderTest.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void testDoFilterInternal_withEmptyRefreshTokenValue_skipsAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME))).thenReturn(Optional.of(refreshTokenCookie));
        when(refreshTokenCookie.getValue()).thenReturn("");

        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getCredentials()).thenReturn(VALID_API_KEY);

        // Act
        classUnderTest.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void testDoFilterInternal_withNonBearerToken_skipsAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("Basic " + VALID_TOKEN);
        when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME))).thenReturn(Optional.of(refreshTokenCookie));
        when(refreshTokenCookie.getValue()).thenReturn(VALID_REFRESH_TOKEN);

        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getCredentials()).thenReturn(VALID_API_KEY);

        // Act
        classUnderTest.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void testDoFilterInternal_withMissingCurrentAuth_skipsAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME))).thenReturn(Optional.of(refreshTokenCookie));

        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        classUnderTest.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void testDoFilterInternal_withNonStringCredentials_skipsAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME))).thenReturn(Optional.of(refreshTokenCookie));

        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getCredentials()).thenReturn(123); // Non-string credentials

        // Act
        classUnderTest.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void testDoFilterInternal_whenUserNotFound_continuesFilterChain() {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME))).thenReturn(Optional.of(refreshTokenCookie));
        when(refreshTokenCookie.getValue()).thenReturn(VALID_REFRESH_TOKEN);

        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getCredentials()).thenReturn(VALID_API_KEY);

        when(jwtTokenService.extractUsername(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(userService.getUserFromEmail(TEST_USERNAME)).thenThrow(new UsernameNotFoundException("User not found"));

        // Act
        assertThrows(UsernameNotFoundException.class, () -> classUnderTest.doFilterInternal(request, response, filterChain));

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void testDoFilterInternal_whenServletExceptionThrown_propagatesException() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME))).thenReturn(Optional.of(refreshTokenCookie));
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        final ServletException servletException = new ServletException("Test exception");
        doThrow(servletException).when(filterChain).doFilter(request, response);

        // Act & Assert
        assertThrows(ServletException.class, () ->
                classUnderTest.doFilterInternal(request, response, filterChain)
        );
    }

    @Test
    void testDoFilterInternal_whenIOExceptionThrown_propagatesException() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME))).thenReturn(Optional.of(refreshTokenCookie));
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        final IOException ioException = new IOException("Test IO exception");
        doThrow(ioException).when(filterChain).doFilter(request, response);

        // Act & Assert
        assertThrows(IOException.class, () ->
                classUnderTest.doFilterInternal(request, response, filterChain)
        );
    }
}
