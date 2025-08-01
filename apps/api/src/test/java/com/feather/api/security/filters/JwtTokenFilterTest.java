package com.feather.api.security.filters;

import static com.feather.api.shared.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.feather.api.shared.AuthenticationConstants.BEARER_PREFIX;
import static com.feather.api.shared.AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.security.exception_handling.FeatherAuthenticationEntryPoint;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.security.tokens.ApiKeyAuthenticationToken;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import com.feather.api.service.CookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

    private static final String API_KEY = "test-api-key";
    private static final String ACCESS_TOKEN = BEARER_PREFIX + "test.access.token";
    private static final String REFRESH_TOKEN = "test.refresh.token";
    private static final String NEW_ACCESS_TOKEN = "new.access.token";

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private CookieService cookieService;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private FeatherAuthenticationEntryPoint authenticationEntryPoint;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private ApiKeyAuthenticationToken currentAuthentication;
    @Mock
    private User mockUser;
    @Mock
    private Cookie refreshTokenCookie;
    @Mock
    private Cookie accessTokenCookie;
    private Cookie[] cookies;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

    @AfterEach
    void tearDown() {
        securityContextHolderMockedStatic.close();
    }

    @InjectMocks
    private JwtTokenFilter classUnderTest;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        cookies = new Cookie[] { refreshTokenCookie, accessTokenCookie };
        when(request.getCookies()).thenReturn(cookies);
        securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @Nested
    class SuccessfulAuthentication {

        @BeforeEach
        void setUp() {
            when(cookieService.findCookie(cookies, REFRESH_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(refreshTokenCookie));
            when(cookieService.findCookie(cookies, ACCESS_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(accessTokenCookie));
            when(refreshTokenCookie.getValue()).thenReturn(REFRESH_TOKEN);
            when(accessTokenCookie.getValue()).thenReturn(ACCESS_TOKEN);
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);
            when(jwtTokenService.loadUserFromToken(ACCESS_TOKEN)).thenReturn(mockUser);
        }

        @Test
        void whenValidTokens_ShouldAuthenticateAndContinueChain() throws Exception {
            final FeatherCredentials expectedCredentials = new FeatherCredentials(API_KEY, ACCESS_TOKEN, REFRESH_TOKEN);
            when(authenticationManager.authenticate(any())).thenReturn(new FeatherAuthenticationToken(mockUser, expectedCredentials));
            classUnderTest.doFilterInternal(request, response, filterChain);
            verify(securityContext).setAuthentication(any(Authentication.class));
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void whenAccessTokenUpdated_ShouldSetCookie() throws Exception {
            final FeatherCredentials updatedCredentials = new FeatherCredentials(API_KEY, NEW_ACCESS_TOKEN, REFRESH_TOKEN);
            when(authenticationManager.authenticate(any())).thenReturn(new FeatherAuthenticationToken(mockUser, updatedCredentials));
            Cookie newCookie = new Cookie("accessToken", NEW_ACCESS_TOKEN);
            when(cookieService.createCookie(any(), any(), any())).thenReturn(newCookie);
            classUnderTest.doFilterInternal(request, response, filterChain);
            verify(response).addCookie(newCookie);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    class ErrorHandling {

        @Test
        void whenBadCredentialsExceptionAndLinkedinUrl_ShouldContinueChain() throws Exception {
            when(request.getRequestURI()).thenReturn("/auth/linkedin/loginUrl");
            doThrow(new BadCredentialsException("bad creds")).when(cookieService).findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME));
            classUnderTest.doFilterInternal(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void whenBadCredentialsExceptionAndOtherUrl_ShouldThrow() {
            when(request.getRequestURI()).thenReturn("/other/url");
            doThrow(new BadCredentialsException("bad creds")).when(cookieService).findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME));
            try {
                classUnderTest.doFilterInternal(request, response, filterChain);
            } catch (BadCredentialsException | ServletException | IOException e) {
                assertThat(e.getMessage()).contains("Refresh token cookie not found");
            }
        }

        @Test
        void whenJwtAuthenticationException_ShouldClearContextAndHandleError() throws Exception {
            when(cookieService.findCookie(cookies, REFRESH_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(refreshTokenCookie));
            when(cookieService.findCookie(cookies, ACCESS_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(accessTokenCookie));
            when(refreshTokenCookie.getValue()).thenReturn(REFRESH_TOKEN);
            when(accessTokenCookie.getValue()).thenReturn(ACCESS_TOKEN);
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);
            doThrow(new JwtAuthenticationException("Invalid token")).when(jwtTokenService).loadUserFromToken(any());
            classUnderTest.doFilterInternal(request, response, filterChain);
            verify(authenticationEntryPoint).commence(eq(request), eq(response), any(JwtAuthenticationException.class));
            securityContextHolderMockedStatic.verify(SecurityContextHolder::clearContext);
        }

        @Test
        void whenUsernameNotFoundException_ShouldClearContextAndHandleError() throws Exception {
            when(cookieService.findCookie(cookies, REFRESH_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(refreshTokenCookie));
            when(cookieService.findCookie(cookies, ACCESS_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(accessTokenCookie));
            when(refreshTokenCookie.getValue()).thenReturn(REFRESH_TOKEN);
            when(accessTokenCookie.getValue()).thenReturn(ACCESS_TOKEN);
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);
            doThrow(new UsernameNotFoundException("User not found")).when(jwtTokenService).loadUserFromToken(any());
            classUnderTest.doFilterInternal(request, response, filterChain);
            verify(authenticationEntryPoint).commence(eq(request), eq(response), any(UsernameNotFoundException.class));
            securityContextHolderMockedStatic.verify(SecurityContextHolder::clearContext);
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void whenMissingApiKey_ShouldContinueChainWithoutAuthentication() throws Exception {
            when(cookieService.findCookie(cookies, REFRESH_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(refreshTokenCookie));
            when(cookieService.findCookie(cookies, ACCESS_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(accessTokenCookie));
            securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);
            classUnderTest.doFilterInternal(request, response, filterChain);
            verify(authenticationManager, never()).authenticate(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void whenRefreshTokenCookieMissing_ShouldHandleBadCredentialsException() throws Exception {
            when(cookieService.findCookie(cookies, REFRESH_TOKEN_COOKIE_NAME)).thenReturn(Optional.empty());
            when(cookieService.findCookie(cookies, ACCESS_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(accessTokenCookie));
            try {
                classUnderTest.doFilterInternal(request, response, filterChain);
            } catch (BadCredentialsException e) {
                assertThat(e.getMessage()).contains("Refresh token cookie not found");
            }
        }

        @Test
        void whenRefreshTokenCookieMissingAndLinkedinUrl_ShouldContinueChain() throws Exception {
            when(cookieService.findCookie(cookies, REFRESH_TOKEN_COOKIE_NAME)).thenReturn(Optional.empty());
            when(cookieService.findCookie(cookies, ACCESS_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(accessTokenCookie));
            classUnderTest.doFilterInternal(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void whenRefreshTokenIsEmpty_ShouldSkipAuthentication() throws Exception {
            when(cookieService.findCookie(cookies, REFRESH_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(refreshTokenCookie));
            when(cookieService.findCookie(cookies, ACCESS_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(accessTokenCookie));
            when(refreshTokenCookie.getValue()).thenReturn("");
            when(accessTokenCookie.getValue()).thenReturn(ACCESS_TOKEN);
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);
            classUnderTest.doFilterInternal(request, response, filterChain);
            verify(authenticationManager, never()).authenticate(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void whenAccessTokenIsEmpty_ShouldSkipAuthentication() throws Exception {
            when(cookieService.findCookie(cookies, REFRESH_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(refreshTokenCookie));
            when(cookieService.findCookie(cookies, ACCESS_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(accessTokenCookie));
            when(refreshTokenCookie.getValue()).thenReturn(REFRESH_TOKEN);
            when(accessTokenCookie.getValue()).thenReturn("");
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);
            classUnderTest.doFilterInternal(request, response, filterChain);
            verify(authenticationManager, never()).authenticate(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void whenRefreshTokenCookiePresentButAccessTokenCookieMissing_ShouldSkipAuthentication() throws Exception {
            when(cookieService.findCookie(cookies, REFRESH_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(refreshTokenCookie));
            when(cookieService.findCookie(cookies, ACCESS_TOKEN_COOKIE_NAME)).thenReturn(Optional.empty());
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);
            classUnderTest.doFilterInternal(request, response, filterChain);
            verify(authenticationManager, never()).authenticate(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    class TokenUpdate {

        @Test
        void whenAccessTokenSame_ShouldNotUpdateCookie() throws Exception {
            when(cookieService.findCookie(cookies, REFRESH_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(refreshTokenCookie));
            when(cookieService.findCookie(cookies, ACCESS_TOKEN_COOKIE_NAME)).thenReturn(Optional.of(accessTokenCookie));
            when(refreshTokenCookie.getValue()).thenReturn(REFRESH_TOKEN);
            when(accessTokenCookie.getValue()).thenReturn(ACCESS_TOKEN);
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);
            final FeatherCredentials sameCredentials = new FeatherCredentials(API_KEY, ACCESS_TOKEN, REFRESH_TOKEN);
            when(jwtTokenService.loadUserFromToken(ACCESS_TOKEN)).thenReturn(mockUser);
            when(authenticationManager.authenticate(any())).thenReturn(new FeatherAuthenticationToken(mockUser, sameCredentials));
            classUnderTest.doFilterInternal(request, response, filterChain);
            verify(response, never()).addCookie(any());
            verify(filterChain).doFilter(request, response);
        }
    }
}
