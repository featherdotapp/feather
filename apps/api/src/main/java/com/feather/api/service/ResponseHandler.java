package com.feather.api.service;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;
import static com.feather.api.shared.TokenType.REFRESH_TOKEN;

import java.io.IOException;

import com.feather.api.security.exception_handling.FeatherAuthenticationEntryPoint;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import com.feather.api.security.tokens.credentials.JwtTokenCredentials;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Class in charge of handling user responses
 */
@Component
@RequiredArgsConstructor
public class ResponseHandler {

    @Value("${security.jwt.access-expiration-time}")
    private String accessTokenExpirationTime;

    @Value("${security.jwt.refresh-expiration-time}")
    private String refreshTokenExpirationTime;

    @Value("${frontend.url}")
    private String frontendUrl;

    private final CookieService cookieService;
    private final FeatherAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * Adds updated access and/or refresh token cookies to the response if they have changed.
     *
     * @param response          the HTTP response to modify
     * @param credentials       the original token credentials
     * @param updatedCredentials the updated token credentials to compare against
     */
    public void updateTokenCookiesIfChanged(final HttpServletResponse response, final FeatherCredentials credentials,
            final FeatherCredentials updatedCredentials) {
        if (!credentials.accessToken().equals(updatedCredentials.accessToken())) {
            response.addCookie(cookieService.createCookie(ACCESS_TOKEN.getCookieName(), updatedCredentials.accessToken(), accessTokenExpirationTime));
        }
        if (!credentials.refreshToken().equals(updatedCredentials.refreshToken())) {
            response.addCookie(cookieService.createCookie(REFRESH_TOKEN.getCookieName(), updatedCredentials.refreshToken(), refreshTokenExpirationTime));
        }
    }

    /**
     * Handles authentication failure by clearing the security context and delegating to the authentication entry point.
     *
     * @param request   the HTTP request
     * @param response  the HTTP response
     * @param e         the authentication exception that caused the failure
     * @throws IOException if an I/O error occurs during handling
     */
    public void handleFailureResponse(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException e)
            throws IOException {
        SecurityContextHolder.clearContext();
        authenticationEntryPoint.commence(request, response, e);
    }

    /**
     * Redirects the user to the frontend URL after successful registration,
     * while setting the refresh token as a cookie and the access token in the response header.
     *
     * @param response the HTTP response object
     * @param tokens the JWT token credentials containing access and refresh tokens
     * @throws IOException if an input or output exception occurs during redirection
     */
    public void registerRedirect(final HttpServletResponse response, final JwtTokenCredentials tokens) throws IOException {
        final Cookie refreshTokenCookie = cookieService.createCookie(REFRESH_TOKEN.getCookieName(), tokens.refreshToken(), refreshTokenExpirationTime);
        final Cookie accessTokenCookie = cookieService.createCookie(ACCESS_TOKEN.getCookieName(), tokens.accessToken(), accessTokenExpirationTime);
        response.addCookie(refreshTokenCookie);
        response.addCookie(accessTokenCookie);
        response.sendRedirect(frontendUrl);
    }
}
