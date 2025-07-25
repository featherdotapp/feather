package com.feather.api.jpa.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.feather.api.jpa.model.User;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.shared.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service in charge of handling JWT Token logic
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Getter
    @Value("${security.jwt.acess-expiration-time}")
    private long accessTokenExpiration;

    @Getter
    @Value("${security.jwt.refresh-expiration-time}")
    private long refreshTokenExpiration;

    /**
     * Generates a JWT for a user.
     *
     * @param userDetails The user details.
     * @param tokenType The token to generate {@link TokenType#ACCESS_TOKEN ACCESS_TOKEN} or {@link TokenType#REFRESH_TOKEN REFRESH_TOKEN}
     * @return The JWT.
     */
    public String generateJwtToken(final UserDetails userDetails, final TokenType tokenType) {
        if (tokenType == TokenType.ACCESS_TOKEN) {
            final List<String> userRoles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            final Map<String, List<String>> extraClaims = new HashMap<>(Map.of(
                    "roles", userRoles
            ));
            return buildToken(extraClaims, userDetails, accessTokenExpiration);
        }
        return buildToken(new HashMap<>(), userDetails, refreshTokenExpiration);
    }

    private String buildToken(final Map<String, List<String>> extraClaims, final UserDetails userDetails, final long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts the username from a JWT.
     *
     * @param token The JWT.
     * @return The username.
     */
    public String extractUsername(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(final String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        final byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

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
        if (!isTokenExpired(accessToken)) {
            return accessToken;
        }
        if (isTokenExpired(refreshToken)) {
            throw new JwtAuthenticationException("Expired Refresh Token, log in again to get a new Refresh Token.");
        }
        return generateJwtToken(user, TokenType.ACCESS_TOKEN);
    }

    private void validateTokensOrThrow(final String accessToken, final String refreshToken, final User user) throws JwtAuthenticationException {
        if (isJwtTokenInvalid(accessToken, user, TokenType.ACCESS_TOKEN)) {
            throw new JwtAuthenticationException("Access Token is invalid");
        }
        if (isJwtTokenInvalid(refreshToken, user, TokenType.REFRESH_TOKEN)) {
            throw new JwtAuthenticationException("Invalid Refresh Token");
        }
    }

    private boolean isJwtTokenInvalid(final String token, final User user, final TokenType tokenType) {
        final String username = extractUsername(token);
        return (!username.equals(user.getUsername())) || !isTokenValidForUser(token, user, tokenType);
    }

    private boolean isTokenValidForUser(final String accessToken, final User user, final TokenType tokenType) {
        return switch (tokenType) {
            case ACCESS_TOKEN -> Objects.equals(accessToken, user.getAccessToken());
            case REFRESH_TOKEN -> Objects.equals(accessToken, user.getRefreshToken());
        };
    }

    private boolean isTokenExpired(final String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

}
