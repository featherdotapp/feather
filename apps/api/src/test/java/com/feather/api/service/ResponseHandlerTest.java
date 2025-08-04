package com.feather.api.service;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;
import static com.feather.api.shared.TokenType.REFRESH_TOKEN;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.feather.api.security.exception_handling.FeatherAuthenticationEntryPoint;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import com.feather.api.security.tokens.credentials.JwtTokenCredentials;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ResponseHandlerTest {

    private static final String REFRESH_EXPIRATION_TIME = "604800";
    private static final String ACCESS_EXPIRATION_TIME = "3600";
    private static final String FRONTEND_URL = "http://localhost:3000";
    private static final String EXPECTED_EXPIRY = "604800";

    @Mock
    private CookieService cookieService;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FeatherAuthenticationEntryPoint authenticationEntryPoint;

    @InjectMocks
    private ResponseHandler classUnderTest;

    @Nested
    class CookiesRelatedTests {

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(classUnderTest, "refreshTokenExpirationTime", REFRESH_EXPIRATION_TIME);
            ReflectionTestUtils.setField(classUnderTest, "accessTokenExpirationTime", ACCESS_EXPIRATION_TIME);
            ReflectionTestUtils.setField(classUnderTest, "frontendUrl", FRONTEND_URL);
        }

        @Test
        void testRegisterRedirect() throws IOException {
            // Arrange
            final String accessToken = "test-access-token";
            final String refreshToken = "test-refresh-token";

            final JwtTokenCredentials tokens = new JwtTokenCredentials(accessToken, refreshToken);
            final Cookie refreshCookie = new Cookie(REFRESH_TOKEN.getCookieName(), refreshToken);
            final Cookie accessCookie = new Cookie(ACCESS_TOKEN.getCookieName(), accessToken);

            when(cookieService.createCookie(REFRESH_TOKEN.getCookieName(), refreshToken, EXPECTED_EXPIRY))
                    .thenReturn(refreshCookie);
            when(cookieService.createCookie(ACCESS_TOKEN.getCookieName(), accessToken, ACCESS_EXPIRATION_TIME))
                    .thenReturn(accessCookie);

            // Act
            classUnderTest.registerRedirect(response, tokens);

            // Assert
            verify(cookieService).createCookie(REFRESH_TOKEN.getCookieName(), refreshToken, EXPECTED_EXPIRY);
            verify(cookieService).createCookie(ACCESS_TOKEN.getCookieName(), accessToken, ACCESS_EXPIRATION_TIME);
            verify(response).addCookie(refreshCookie);
            verify(response).addCookie(accessCookie);
            verify(response).sendRedirect(FRONTEND_URL);
        }

        @Test
        void testRegisterRedirect_withIOException() throws IOException {
            // Arrange
            final String accessToken = "test-access-token";
            final String refreshToken = "test-refresh-token";

            final JwtTokenCredentials tokens = new JwtTokenCredentials(accessToken, refreshToken);
            final Cookie refreshCookie = new Cookie(REFRESH_TOKEN.getCookieName(), refreshToken);
            final Cookie accessCookie = new Cookie(ACCESS_TOKEN.getCookieName(), accessToken);

            when(cookieService.createCookie(REFRESH_TOKEN.getCookieName(), refreshToken, EXPECTED_EXPIRY))
                    .thenReturn(refreshCookie);
            when(cookieService.createCookie(ACCESS_TOKEN.getCookieName(), accessToken, ACCESS_EXPIRATION_TIME))
                    .thenReturn(accessCookie);
            doThrow(new IOException("CookiesRelatedTests failed")).when(response).sendRedirect(FRONTEND_URL);

            // Act
            assertThrows(IOException.class, () -> classUnderTest.registerRedirect(response, tokens));

            // Assert
            verify(cookieService).createCookie(REFRESH_TOKEN.getCookieName(), refreshToken, EXPECTED_EXPIRY);
            verify(cookieService).createCookie(ACCESS_TOKEN.getCookieName(), accessToken, ACCESS_EXPIRATION_TIME);
            verify(response).addCookie(refreshCookie);
            verify(response).addCookie(accessCookie);
            verify(response).sendRedirect(FRONTEND_URL);
        }

        @Test
        void updateTokenCookiesIfChanged() {
            // Arrange
            final FeatherCredentials providedCredentials = mock(FeatherCredentials.class);
            final FeatherCredentials updatedCredentials = mock(FeatherCredentials.class);

            final String someAccessToken = "someAccess";
            final String someRefreshToken = "someRefresh";

            when(providedCredentials.accessToken()).thenReturn(someAccessToken);
            when(updatedCredentials.accessToken()).thenReturn(someAccessToken);

            when(providedCredentials.refreshToken()).thenReturn(someRefreshToken);
            when(updatedCredentials.refreshToken()).thenReturn(someRefreshToken);

            // Act
            classUnderTest.updateTokenCookiesIfChanged(response, providedCredentials, updatedCredentials);

            // Assert
            verifyNoInteractions(cookieService);
        }

        @Test
        void updateTokenCookiesIfChanged_updatedTokens() {
            // Arrange
            final FeatherCredentials providedCredentials = mock(FeatherCredentials.class);
            final FeatherCredentials updatedCredentials = mock(FeatherCredentials.class);
            final String someAccessToken = "someAccess";
            final String someRefreshToken = "someRefresh";
            final String someOtherAccessToken = "someOtherAccess";
            final String someOtherRefreshToken = "someOtherRefresh";
            final Cookie accesTokenCookie = new Cookie(ACCESS_TOKEN.getCookieName(), someOtherAccessToken);
            final Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN.getCookieName(), someOtherRefreshToken);

            when(providedCredentials.accessToken()).thenReturn(someAccessToken);
            when(updatedCredentials.accessToken()).thenReturn(someOtherAccessToken);
            when(providedCredentials.refreshToken()).thenReturn(someRefreshToken);
            when(updatedCredentials.refreshToken()).thenReturn(someOtherRefreshToken);
            when(cookieService.createCookie(ACCESS_TOKEN.getCookieName(), someOtherAccessToken, ACCESS_EXPIRATION_TIME))
                    .thenReturn(accesTokenCookie);
            when(cookieService.createCookie(REFRESH_TOKEN.getCookieName(), someOtherRefreshToken, REFRESH_EXPIRATION_TIME))
                    .thenReturn(refreshTokenCookie);

            // Act
            classUnderTest.updateTokenCookiesIfChanged(response, providedCredentials, updatedCredentials);

            // Assert
            verify(response).addCookie(accesTokenCookie);
            verify(response).addCookie(refreshTokenCookie);
        }

        @Test
        void updateTokenCookiesIfChanged_updatedAccessToken() {
            // Arrange
            final FeatherCredentials providedCredentials = mock(FeatherCredentials.class);
            final FeatherCredentials updatedCredentials = mock(FeatherCredentials.class);
            final String someAccessToken = "someAccess";
            final String someRefreshToken = "someRefresh";
            final String someOtherAccessToken = "someOtherAccess";
            final Cookie accesTokenCookie = new Cookie(ACCESS_TOKEN.getCookieName(), someOtherAccessToken);

            when(providedCredentials.accessToken()).thenReturn(someAccessToken);
            when(updatedCredentials.accessToken()).thenReturn(someOtherAccessToken);
            when(providedCredentials.refreshToken()).thenReturn(someRefreshToken);
            when(updatedCredentials.refreshToken()).thenReturn(someRefreshToken);
            when(cookieService.createCookie(ACCESS_TOKEN.getCookieName(), someOtherAccessToken, ACCESS_EXPIRATION_TIME))
                    .thenReturn(accesTokenCookie);

            // Act
            classUnderTest.updateTokenCookiesIfChanged(response, providedCredentials, updatedCredentials);

            // Assert
            verify(response).addCookie(accesTokenCookie);
        }

        @Test
        void updateTokenCookiesIfChanged_updatedRefreshToken() {
            // Arrange
            final FeatherCredentials providedCredentials = mock(FeatherCredentials.class);
            final FeatherCredentials updatedCredentials = mock(FeatherCredentials.class);
            final String someAccessToken = "someAccess";
            final String someRefreshToken = "someRefresh";
            final String someOtherRefreshToken = "someOtherRefresh";
            final Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN.getCookieName(), someOtherRefreshToken);

            when(providedCredentials.accessToken()).thenReturn(someAccessToken);
            when(updatedCredentials.accessToken()).thenReturn(someAccessToken);
            when(providedCredentials.refreshToken()).thenReturn(someRefreshToken);
            when(updatedCredentials.refreshToken()).thenReturn(someOtherRefreshToken);
            when(cookieService.createCookie(REFRESH_TOKEN.getCookieName(), someOtherRefreshToken, REFRESH_EXPIRATION_TIME))
                    .thenReturn(refreshTokenCookie);

            // Act
            classUnderTest.updateTokenCookiesIfChanged(response, providedCredentials, updatedCredentials);

            // Assert
            verify(response).addCookie(refreshTokenCookie);
        }

    }

    @Nested
    class FailureResponse {

        @Mock
        private HttpServletRequest request;
        private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

        @BeforeEach
        void setUp() {
            securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);
        }

        @AfterEach
        void tearDown() {
            securityContextHolderMockedStatic.close();
        }

        @Test
        void handleFailureResponse() throws IOException {
            // Arrange
            final String someException = "SOME EXCEPTION";
            final JwtAuthenticationException e = new JwtAuthenticationException(someException);

            // Act
            classUnderTest.handleFailureResponse(request, response, e);

            // Assert
            securityContextHolderMockedStatic.verify(SecurityContextHolder::clearContext);
            verify(authenticationEntryPoint).commence(request, response, e);
        }
    }
}