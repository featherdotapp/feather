package com.feather.api.service.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.feather.api.shared.TokenType;
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
 * Class in charge of building JWT Tokens
 */
@Service
@RequiredArgsConstructor
public class JwtTokenBuilder {

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
    public String buildToken(final UserDetails userDetails, final TokenType tokenType) {
        final TokenDetails tokenDetails = createTokenDetails(userDetails, tokenType);
        return Jwts
                .builder()
                .setClaims(tokenDetails.extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenDetails.expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private TokenDetails createTokenDetails(final UserDetails userDetails, final TokenType tokenType) {
        final long expiration;
        final Map<String, Object> extraClaims = new HashMap<>();
        if (tokenType == TokenType.ACCESS_TOKEN) {
            final List<String> userRoles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            extraClaims.put("roles", userRoles);
            expiration = accessTokenExpiration;
        } else {
            expiration = refreshTokenExpiration;
        }
        return new TokenDetails(expiration, extraClaims);
    }

    /**
     * Retrieves the signing key used for generating and validating JWT tokens.
     *
     * @return The signing key derived from the secret key.
     */
    protected Key getSignInKey() {
        final byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private record TokenDetails(long expiration, Map<String, Object> extraClaims) {

    }

}
