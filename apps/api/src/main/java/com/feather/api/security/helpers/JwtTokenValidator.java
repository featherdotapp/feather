package com.feather.api.security.helpers;

import static com.feather.api.security.exception_handling.exception.JwtAuthenticationException.EXPIRED_REFRESH_TOKEN;
import static com.feather.api.security.exception_handling.exception.JwtAuthenticationException.INVALID_ACCESS_TOKEN;
import static com.feather.api.security.exception_handling.exception.JwtAuthenticationException.INVALID_REFRESH_TOKEN;

import java.util.Date;
import java.util.Objects;

import com.feather.api.jpa.model.User;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.service.jwt.JwtTokenBuilder;
import com.feather.api.service.jwt.JwtTokenParser;
import com.feather.api.shared.TokenType;
import lombok.RequiredArgsConstructor;
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

    private final JwtTokenBuilder jwtTokenBuilder;
    private final JwtTokenParser jwtTokenParser;

    /**
     * Validates the access token and refreshes it if expired, and the refresh token is valid.
     * Throws JwtAuthenticationException if any token is invalid or if the refresh token is expired
     *
     * @param accessToken The access token.
     * @param refreshToken The refresh token.
     * @param user The user.
     * @return A valid access token (new or existing).
     */
    public String validateOrRefreshAccessToken(final String accessToken, final String refreshToken, final User user) throws JwtAuthenticationException {
        validateTokensOrThrow(accessToken, refreshToken, user);
        if (isTokenExpired(refreshToken)) {
            throw new JwtAuthenticationException(EXPIRED_REFRESH_TOKEN);
        }
        if (!isTokenExpired(accessToken)) {
            return accessToken;
        }
        return jwtTokenBuilder.buildToken(user, TokenType.ACCESS_TOKEN);
    }

    private void validateTokensOrThrow(final String accessToken, final String refreshToken, final User user) throws JwtAuthenticationException {
        if (isJwtTokenInvalid(refreshToken, user, TokenType.REFRESH_TOKEN)) {
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
}
