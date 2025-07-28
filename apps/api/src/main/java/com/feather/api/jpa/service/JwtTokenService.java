package com.feather.api.jpa.service;

import static com.feather.api.security.exception_handling.exception.JwtAuthenticationException.EXPIRED_REFRESH_TOKEN;
import static com.feather.api.shared.AuthenticationConstants.BEARER_PREFIX;

import com.feather.api.jpa.model.User;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.service.jwt.JwtTokenParser;
import com.feather.api.shared.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service in charge of handling JWT Token logic
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtTokenParser jwtTokenParser;
    private final UserService userService;

    /**
     * Retrieves the UserDetails from a {@link TokenType#ACCESS_TOKEN}
     *
     * @param accessToken the string access token
     * @param refreshToken the string refresh token
     * @return the User implementation of UserDetails
     */
    public User loadUserFromToken(final String accessToken, final String refreshToken) throws UsernameNotFoundException {
        try {
            final String token = accessToken.substring(BEARER_PREFIX.length());
            final String userName = jwtTokenParser.extractClaim(token, Claims::getSubject);
            return userService.getUserFromEmail(userName);
        } catch (final ExpiredJwtException e) {
            // The user is extracted here from the refresh token, and the invalidation of the jwt access token will be checked in the authentication provider
            return extractUsernameFromRefreshToken(refreshToken);
        }
    }

    private User extractUsernameFromRefreshToken(final String refreshToken) throws UsernameNotFoundException {
        try {
            final String userName = jwtTokenParser.extractClaim(refreshToken, Claims::getSubject);
            return userService.getUserFromEmail(userName);
        } catch (final ExpiredJwtException e) {
            throw new JwtAuthenticationException(EXPIRED_REFRESH_TOKEN);
        }
    }

}
