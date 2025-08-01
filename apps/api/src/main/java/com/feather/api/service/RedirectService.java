package com.feather.api.service;

import java.io.IOException;

import com.feather.api.security.tokens.credentials.JwtTokenCredentials;
import com.feather.api.shared.AuthenticationConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service responsible for handling redirection logic, including setting cookies
 * and headers for authentication tokens.
 */
@Service
@RequiredArgsConstructor
public class RedirectService {

    private final CookieService cookieService;
    @Value("${security.jwt.refresh-expiration-time}")
    private String refreshExpirationTime;
    @Value("${security.jwt.access-expiration-time}")
    private String accessTokenExpirationTime;
    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * Redirects the user to the frontend URL after successful registration,
     * while setting the refresh token as a cookie and the access token in the response header.
     *
     * @param response the HTTP response object
     * @param tokens the JWT token credentials containing access and refresh tokens
     * @throws IOException if an input or output exception occurs during redirection
     */
    public void registerRedirect(final HttpServletResponse response, final JwtTokenCredentials tokens) throws IOException {
        final Cookie refreshTokenCookie =
                cookieService.createCookie(AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME, tokens.refreshToken(), refreshExpirationTime);
        final Cookie accessTokenCookie =
                cookieService.createCookie(AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME, tokens.accessToken(), accessTokenExpirationTime);
        response.addCookie(refreshTokenCookie);
        response.addCookie(accessTokenCookie);
        response.sendRedirect(frontendUrl);
    }

}
