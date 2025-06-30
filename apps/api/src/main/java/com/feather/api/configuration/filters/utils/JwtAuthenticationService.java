package com.feather.api.configuration.filters.utils;

import com.feather.api.jpa.service.JwtTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Service for handling JWT authentication and token validation.
 */
@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

    private final JwtTokenService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Authenticates a user based on the provided access token and request.
     *
     * @param request the HTTP request containing cookies
     * @param accessToken the JWT access token
     * @return an {@link AuthResult} indicating the authentication result
     */
    public AuthResult authenticate(final HttpServletRequest request, final String accessToken) {
        final String userEmail = jwtService.extractUsername(accessToken);

        if (userEmail == null) {
            return AuthResult.invalid("Invalid JWT Token");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if (!jwtService.isTokenExpired(accessToken)) {
            if (jwtService.isTokenValid(accessToken, userDetails)) {
                return AuthResult.success(userDetails);
            } else {
                return AuthResult.invalid("Invalid JWT Token");
            }
        } else {
            final String refreshToken = getRefreshTokenFromCookies(request.getCookies());
            if (refreshToken != null) {
                final String refreshUserEmail = jwtService.extractUsername(refreshToken);
                final UserDetails refreshUserDetails = userDetailsService.loadUserByUsername(refreshUserEmail);
                if (jwtService.isTokenValid(refreshToken, refreshUserDetails)) {
                    final String newAccessToken = jwtService.generateAccessToken(refreshUserDetails);
                    return AuthResult.refreshed(newAccessToken);
                }
            }
            return AuthResult.invalid("Refresh token expired or invalid. Please log in again.");
        }
    }

    private String getRefreshTokenFromCookies(final Cookie[] cookies) {
        if (cookies == null)
            return null;
        for (final Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}

