package com.feather.api.security.filters;

import static com.feather.api.shared.AuthenticationConstants.AUTHORIZATION_HEADER;
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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

    @Captor
    private ArgumentCaptor<Authentication> authenticationCaptor;

    @InjectMocks
    private JwtTokenFilter classUnderTest;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    class SuccessfulAuthentication {

        @BeforeEach
        void setUp() {
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(ACCESS_TOKEN);
            when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME)))
                    .thenReturn(Optional.of(refreshTokenCookie));
            when(refreshTokenCookie.getValue()).thenReturn(REFRESH_TOKEN);
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);
            when(jwtTokenService.loadUserFromToken(ACCESS_TOKEN)).thenReturn(mockUser);
        }

        @Test
        void whenValidTokens_ShouldAuthenticateAndContinueChain() throws Exception {
            // Arrange
            final FeatherCredentials expectedCredentials = new FeatherCredentials(API_KEY,
                    ACCESS_TOKEN.substring(BEARER_PREFIX.length()),
                    REFRESH_TOKEN);
            when(authenticationManager.authenticate(any())).thenAnswer(invocation ->
                    new FeatherAuthenticationToken(mockUser, expectedCredentials)
            );

            // Act
            classUnderTest.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityContext).setAuthentication(authenticationCaptor.capture());
            final Authentication resultAuth = authenticationCaptor.getValue();
            assertThat(((FeatherCredentials) resultAuth.getCredentials()).accessToken())
                    .isEqualTo(expectedCredentials.accessToken());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void whenAccessTokenUpdated_ShouldSetResponseHeader() throws Exception {
            // Arrange
            final FeatherCredentials updatedCredentials = new FeatherCredentials(API_KEY, NEW_ACCESS_TOKEN, REFRESH_TOKEN);
            when(authenticationManager.authenticate(any()))
                    .thenReturn(new FeatherAuthenticationToken(mockUser, updatedCredentials));

            // Act
            classUnderTest.doFilterInternal(request, response, filterChain);

            // Assert
            verify(response).setHeader(AUTHORIZATION_HEADER, NEW_ACCESS_TOKEN);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    class FailedAuthentication {

        private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

        @BeforeEach
        void setUp() {
            securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class, Answers.RETURNS_DEEP_STUBS);
        }

        @AfterEach
        void tearDown() {
            securityContextHolderMockedStatic.close();
        }

        @Test
        void whenMissingAccessToken_ShouldContinueChainWithoutAuthentication() throws Exception {
            // Arrange
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);
            securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);

            // Act
            classUnderTest.doFilterInternal(request, response, filterChain);

            // Assert
            verify(authenticationManager, never()).authenticate(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void whenMissingRefreshToken_ShouldContinueChainWithoutAuthentication() throws Exception {
            // Arrange
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(ACCESS_TOKEN);
            when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME)))
                    .thenReturn(Optional.empty());
            securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);

            // Act
            classUnderTest.doFilterInternal(request, response, filterChain);

            // Assert
            verify(authenticationManager, never()).authenticate(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void whenJwtAuthenticationException_ShouldClearContextAndHandleError() throws Exception {
            // Arrange
            setupValidTokens();
            final JwtAuthenticationException exception = new JwtAuthenticationException("Invalid token");
            doThrow(exception).when(jwtTokenService).loadUserFromToken(any());

            // Act
            classUnderTest.doFilterInternal(request, response, filterChain);

            // Assert
            securityContextHolderMockedStatic.verify(SecurityContextHolder::clearContext);
            verify(authenticationEntryPoint).commence(request, response, exception);
        }

        @Test
        void whenUsernameNotFoundException_ShouldClearContextAndHandleError() throws Exception {
            // Arrange
            setupValidTokens();
            final UsernameNotFoundException exception = new UsernameNotFoundException("User not found");
            when(jwtTokenService.loadUserFromToken(any())).thenThrow(exception);

            // Act
            classUnderTest.doFilterInternal(request, response, filterChain);

            // Assert
            securityContextHolderMockedStatic.verify(SecurityContextHolder::clearContext);
            verify(authenticationEntryPoint).commence(request, response, exception);
        }

        private void setupValidTokens() {
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(ACCESS_TOKEN);
            when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME)))
                    .thenReturn(Optional.of(refreshTokenCookie));
            when(refreshTokenCookie.getValue()).thenReturn(REFRESH_TOKEN);
            securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);
        }

    }

    @Nested
    class TokenValidation {

        @Test
        void whenEmptyRefreshToken_ShouldSkipAuthentication() throws Exception {
            // Arrange
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(ACCESS_TOKEN);
            when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME)))
                    .thenReturn(Optional.of(refreshTokenCookie));
            when(refreshTokenCookie.getValue()).thenReturn("");
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);

            // Act
            classUnderTest.doFilterInternal(request, response, filterChain);

            // Assert
            verify(authenticationManager, never()).authenticate(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void whenAccessTokenWithoutBearerPrefix_ShouldSkipAuthentication() throws Exception {
            // Arrange
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("invalid-token-format");
            when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME)))
                    .thenReturn(Optional.of(refreshTokenCookie));
            when(refreshTokenCookie.getValue()).thenReturn(REFRESH_TOKEN);
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);

            // Act
            classUnderTest.doFilterInternal(request, response, filterChain);

            // Assert
            verify(authenticationManager, never()).authenticate(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void whenNullCookies_ShouldHandleGracefully() throws Exception {
            // Arrange
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(ACCESS_TOKEN);
            when(cookieService.findCookie(null, REFRESH_TOKEN_COOKIE_NAME))
                    .thenReturn(Optional.empty());
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);

            // Act
            classUnderTest.doFilterInternal(request, response, filterChain);

            // Assert
            verify(authenticationManager, never()).authenticate(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    class TokenUpdate {

        @BeforeEach
        void setUp() {
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(ACCESS_TOKEN);
            when(cookieService.findCookie(any(), eq(REFRESH_TOKEN_COOKIE_NAME)))
                    .thenReturn(Optional.of(refreshTokenCookie));
            when(refreshTokenCookie.getValue()).thenReturn(REFRESH_TOKEN);
            when(securityContext.getAuthentication()).thenReturn(currentAuthentication);
            when(currentAuthentication.getCredentials()).thenReturn(API_KEY);
            when(jwtTokenService.loadUserFromToken(ACCESS_TOKEN)).thenReturn(mockUser);
        }

        @Test
        void whenAccessTokenSame_ShouldNotUpdateHeader() throws Exception {
            // Arrange
            final FeatherCredentials sameCredentials = new FeatherCredentials(API_KEY,
                    ACCESS_TOKEN.substring(BEARER_PREFIX.length()),
                    REFRESH_TOKEN);
            when(authenticationManager.authenticate(any()))
                    .thenReturn(new FeatherAuthenticationToken(mockUser, sameCredentials));

            // Act
            classUnderTest.doFilterInternal(request, response, filterChain);

            // Assert
            verify(response, never()).setHeader(eq(AUTHORIZATION_HEADER), any());
            verify(filterChain).doFilter(request, response);
        }
    }
}

