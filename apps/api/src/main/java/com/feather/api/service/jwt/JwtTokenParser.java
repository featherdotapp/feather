package com.feather.api.service.jwt;

import java.util.function.Function;

import io.jsonwebtoken.Claims;
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
     * Extracts a specific claim from the provided JWT token.
     *
     * @param token The JWT token from which the claim is to be extracted.
     * @param claimsResolver A function to resolve the specific claim from the token's claims.
     * @param <T> The type of the claim to be extracted.
     * @return The extracted claim of type T.
     */
    public <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(final String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(jwtTokenBuilder.getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
