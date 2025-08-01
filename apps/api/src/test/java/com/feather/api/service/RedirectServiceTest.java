package com.feather.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.feather.api.security.tokens.credentials.JwtTokenCredentials;
import com.feather.api.shared.AuthenticationConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RedirectServiceTest {

    private static final String REFRESH_EXPIRATION_TIME = "604800000"; // 7 days in milliseconds
    private static final String FRONTEND_URL = "http://localhost:3000";
    private static final String EXPECTED_EXPIRY = "604800"; // 24 hours in seconds

    @Mock
    private CookieService cookieService;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private RedirectService classUnderTest;

    @Test
    void testRegisterRedirect() throws IOException {
        // Arrange
        final String accessToken = "test-access-token";
        final String refreshToken = "test-refresh-token";

        final JwtTokenCredentials tokens = new JwtTokenCredentials(accessToken, refreshToken);
        final Cookie mockCookie = new Cookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        // Set up the @Value fields using reflection
        ReflectionTestUtils.setField(classUnderTest, "refreshExpirationTime", REFRESH_EXPIRATION_TIME);
        ReflectionTestUtils.setField(classUnderTest, "frontendUrl", FRONTEND_URL);

        when(cookieService.createCookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, EXPECTED_EXPIRY))
                .thenReturn(mockCookie);

        // Act
        classUnderTest.registerRedirect(response, tokens);

        // Assert
        verify(cookieService).createCookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, EXPECTED_EXPIRY);
        verify(response).addCookie(mockCookie);
        verify(response).setHeader(AuthenticationConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken);
        verify(response).sendRedirect(FRONTEND_URL);
    }

    @Test
    void testRegisterRedirect_withIOException() throws IOException {
        // Arrange
        final String accessToken = "test-access-token";
        final String refreshToken = "test-refresh-token";

        final JwtTokenCredentials tokens = new JwtTokenCredentials(accessToken, refreshToken);
        final Cookie mockCookie = new Cookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        ReflectionTestUtils.setField(classUnderTest, "refreshExpirationTime", REFRESH_EXPIRATION_TIME);
        ReflectionTestUtils.setField(classUnderTest, "frontendUrl", FRONTEND_URL);

        when(cookieService.createCookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, EXPECTED_EXPIRY))
                .thenReturn(mockCookie);
        doThrow(new IOException("Redirect failed")).when(response).sendRedirect(FRONTEND_URL);

        // Act & Assert
        assertThrows(IOException.class, () -> classUnderTest.registerRedirect(response, tokens));

        // Verify that cookie and header were still set before the exception
        verify(cookieService).createCookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, EXPECTED_EXPIRY);
        verify(response).addCookie(mockCookie);
        verify(response).setHeader(AuthenticationConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken);
        verify(response).sendRedirect(FRONTEND_URL);
    }

}