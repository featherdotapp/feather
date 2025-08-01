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

    private static final String REFRESH_EXPIRATION_TIME = "604800";
    private static final String ACCESS_EXPIRATION_TIME = "3600";
    private static final String FRONTEND_URL = "http://localhost:3000";
    private static final String EXPECTED_EXPIRY = "604800";

    @Mock
    private CookieService cookieService;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private RedirectService classUnderTest;

    @Test
    void testRegisterRedirect() throws IOException {
        final String accessToken = "test-access-token";
        final String refreshToken = "test-refresh-token";

        final JwtTokenCredentials tokens = new JwtTokenCredentials(accessToken, refreshToken);
        final Cookie refreshCookie = new Cookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        final Cookie accessCookie = new Cookie(AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME, accessToken);

        ReflectionTestUtils.setField(classUnderTest, "refreshExpirationTime", REFRESH_EXPIRATION_TIME);
        ReflectionTestUtils.setField(classUnderTest, "accessTokenExpirationTime", ACCESS_EXPIRATION_TIME);
        ReflectionTestUtils.setField(classUnderTest, "frontendUrl", FRONTEND_URL);

        when(cookieService.createCookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, EXPECTED_EXPIRY))
                .thenReturn(refreshCookie);
        when(cookieService.createCookie(AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME, accessToken, ACCESS_EXPIRATION_TIME))
                .thenReturn(accessCookie);

        classUnderTest.registerRedirect(response, tokens);

        verify(cookieService).createCookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, EXPECTED_EXPIRY);
        verify(cookieService).createCookie(AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME, accessToken, ACCESS_EXPIRATION_TIME);
        verify(response).addCookie(refreshCookie);
        verify(response).addCookie(accessCookie);
        verify(response).sendRedirect(FRONTEND_URL);
    }

    @Test
    void testRegisterRedirect_withIOException() throws IOException {
        final String accessToken = "test-access-token";
        final String refreshToken = "test-refresh-token";

        final JwtTokenCredentials tokens = new JwtTokenCredentials(accessToken, refreshToken);
        final Cookie refreshCookie = new Cookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        final Cookie accessCookie = new Cookie(AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME, accessToken);

        ReflectionTestUtils.setField(classUnderTest, "refreshExpirationTime", REFRESH_EXPIRATION_TIME);
        ReflectionTestUtils.setField(classUnderTest, "accessTokenExpirationTime", ACCESS_EXPIRATION_TIME);
        ReflectionTestUtils.setField(classUnderTest, "frontendUrl", FRONTEND_URL);

        when(cookieService.createCookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, EXPECTED_EXPIRY))
                .thenReturn(refreshCookie);
        when(cookieService.createCookie(AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME, accessToken, ACCESS_EXPIRATION_TIME))
                .thenReturn(accessCookie);
        doThrow(new IOException("Redirect failed")).when(response).sendRedirect(FRONTEND_URL);

        assertThrows(IOException.class, () -> classUnderTest.registerRedirect(response, tokens));

        verify(cookieService).createCookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, EXPECTED_EXPIRY);
        verify(cookieService).createCookie(AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME, accessToken, ACCESS_EXPIRATION_TIME);
        verify(response).addCookie(refreshCookie);
        verify(response).addCookie(accessCookie);
        verify(response).sendRedirect(FRONTEND_URL);
    }

}