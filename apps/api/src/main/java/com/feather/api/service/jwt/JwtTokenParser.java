package com.feather.api.service.jwt;

import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for parsing and extracting claims from JWT tokens.
 * <p>
 * This class provides methods to extract specific claims or all claims from a given JWT token.
 * It relies on the signing key provided by the {@link JwtTokenBuilder} to validate and parse tokens.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class JwtTokenParser {

    private final JwtTokenBuilder jwtTokenBuilder;

    /**
     * Extracts expiration Date from token
     *
     * @param token token to extract expiration from
     * @return the expiration Date
     */
    public Date extractExpirationDate(final String token) {
        try {

            return extractAllClaims(token).getExpiration();
        } catch (final ExpiredJwtException e) {
            return e.getClaims().getExpiration();
        }
    }

    private Claims extractAllClaims(final String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(jwtTokenBuilder.getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts Subject from a token
     *
     * @param token token to extract subject from
     * @return the Subject
     */
    public String extractSubject(final String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (final ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

}
