package com.feather.api.security.helpers.jwt;

import static com.feather.api.security.exception_handling.exception.JwtAuthenticationException.EXPIRED_REFRESH_TOKEN;
import static com.feather.api.security.exception_handling.exception.JwtAuthenticationException.INVALID_ACCESS_TOKEN;
import static com.feather.api.security.exception_handling.exception.JwtAuthenticationException.INVALID_REFRESH_TOKEN;
import static com.feather.api.shared.TokenType.REFRESH_TOKEN;

import java.util.Date;
import java.util.Objects;

import com.feather.api.jpa.model.User;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.service.jwt.JwtTokenParser;
import com.feather.api.shared.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for validating JWT tokens.
 * <p>
 * This class distinguishes between two key validation concepts:
 * </p>
 * <ul>
 *     <li>
 *         <strong>Valid:</strong> A JWT token is considered valid if it matches the token stored in the database
 *         for the current user. For an access token to be regenerated, both the access token and the refresh token
 *         must be valid. Additionally, the access token must be expired, and the refresh token must not be expired.
 *     </li>
 *     <li>
 *         <strong>Expired:</strong> A JWT token is considered expired if its expiration date is in the past.
 *     </li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class JwtTokenValidator {

    @Value("${security.jwt.min-remaining-time-for-refresh-refresh-token}")
    private String minRemainingTimeForRefreshRefreshToken;

    private final JwtTokenParser jwtTokenParser;

    /**
     * Validates if the access token should be updated
     * Throws JwtAuthenticationException if any token is invalid or if the refresh token is expired
     *
     * @param accessToken The access token.
     * @param refreshToken The refresh token.
     * @param user The user.
     * @return true if the token should be updated, false otherwise
     * @throws JwtAuthenticationException if any token is expired or invalid
     */
    protected boolean shouldUpdateAccessToken(final String accessToken, final String refreshToken, final User user) {
        assertTokenBelongsToUserOrThrow(accessToken, refreshToken, user);
        if (isTokenExpired(refreshToken)) {
            throw new JwtAuthenticationException(EXPIRED_REFRESH_TOKEN);
        }
        return isTokenExpired(accessToken);
    }

    private void assertTokenBelongsToUserOrThrow(final String accessToken, final String refreshToken, final User user) throws JwtAuthenticationException {
        if (isJwtTokenInvalid(refreshToken, user, REFRESH_TOKEN)) {
            throw new JwtAuthenticationException(INVALID_REFRESH_TOKEN);
        }
        if (isJwtTokenInvalid(accessToken, user, TokenType.ACCESS_TOKEN)) {
            throw new JwtAuthenticationException(INVALID_ACCESS_TOKEN);
        }
    }

    private boolean isJwtTokenInvalid(final String token, final User user, final TokenType tokenType) {
        final String username = jwtTokenParser.extractSubject(token);
        return (!username.equals(user.getUsername())) || !isTokenValidForUser(token, user, tokenType);
    }

    private boolean isTokenValidForUser(final String accessToken, final User user, final TokenType tokenType) {
        return switch (tokenType) {
            case ACCESS_TOKEN -> Objects.equals(accessToken, user.getAccessToken());
            case REFRESH_TOKEN -> Objects.equals(accessToken, user.getRefreshToken());
        };
    }

    private boolean isTokenExpired(final String token) {
        final Date expiration = jwtTokenParser.extractExpirationDate(token);
        return expiration.before(new Date());
    }

    /**
     * Determines whether a refresh token should be proactively updated based on its remaining validity period.
     * <p>
     * According to the application workflow, if execution reaches this method, the refresh token is assumed to be
     * structurally valid and associated with the current user. Therefore, only expiration-related checks are performed.
     * <p>
     *
     * @param refreshToken the refresh token to evaluate
     * @return {@code true} if the token is valid but nearing expiration and should be refreshed; {@code false} otherwise
     * @throws JwtAuthenticationException if the token is already expired
     */
    protected boolean shouldUpdateRefreshToken(final String refreshToken) {
        if (isTokenExpired(refreshToken)) {
            throw new JwtAuthenticationException(EXPIRED_REFRESH_TOKEN);
        }
        return isTokenExpiringSoon(refreshToken);
    }

    private boolean isTokenExpiringSoon(final String refreshToken) {
        final long minTimeToRefresh = Long.parseLong(minRemainingTimeForRefreshRefreshToken);
        final Date expiration = jwtTokenParser.extractExpirationDate(refreshToken);
        final long timeLeft = expiration.getTime() - System.currentTimeMillis();
        return timeLeft < minTimeToRefresh;
    }
}
