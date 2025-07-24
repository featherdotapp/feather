package com.feather.api.jpa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.feather.api.shared.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 86400000; // 24 hours
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_ROLE = "DEFAULT_USER";

    @Mock
    private UserDetails userDetails;
    @InjectMocks
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
    }

    @Test
    void generateJwtToken_shouldGenerateAccessTokenWithRoles_whenTokenTypeIsAccessToken() {
        // Arrange
        when(userDetails.getUsername()).thenReturn(USER_EMAIL);
        doReturn(Collections.singleton(new SimpleGrantedAuthority(USER_ROLE))).when(userDetails).getAuthorities();

        // Act
        final String token = jwtTokenService.generateJwtToken(userDetails, TokenType.ACCESS_TOKEN);

        // Assert
        final String username = jwtTokenService.extractUsername(token);
        final Claims claims = extractAllClaims(token);

        assertThat(token).isNotEmpty();
        assertThat(username).isEqualTo(USER_EMAIL);
        assertThat(claims.get("roles", List.class)).contains(USER_ROLE);
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isEqualTo(ACCESS_TOKEN_EXPIRATION);
    }

    @Test
    void generateJwtToken_shouldGenerateRefreshTokenWithoutRoles_whenTokenTypeIsRefreshToken() {
        // Arrange
        when(userDetails.getUsername()).thenReturn(USER_EMAIL);

        // Act
        final String token = jwtTokenService.generateJwtToken(userDetails, TokenType.REFRESH_TOKEN);

        // Assert
        final String username = jwtTokenService.extractUsername(token);
        final Claims claims = extractAllClaims(token);

        assertThat(token).isNotEmpty();
        assertThat(username).isEqualTo(USER_EMAIL);
        assertThat(claims.get("roles")).isNull();
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isEqualTo(REFRESH_TOKEN_EXPIRATION);
    }

    @Test
    void isTokenExpired_shouldReturnFalse_whenTokenIsNotExpired() {
        // Arrange
        when(userDetails.getUsername()).thenReturn(USER_EMAIL);
        final String validToken = jwtTokenService.generateJwtToken(userDetails, TokenType.ACCESS_TOKEN);

        // Act & Assert
        final boolean result = jwtTokenService.isTokenExpired(validToken);

        assertThat(result).isFalse();
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        // Arrange
        when(userDetails.getUsername()).thenReturn(USER_EMAIL);
        final String token = jwtTokenService.generateJwtToken(userDetails, TokenType.ACCESS_TOKEN);

        // Act
        final String username = jwtTokenService.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo(USER_EMAIL);
    }

    @Test
    void extractExpiration_shouldReturnCorrectExpirationDate() {
        // Arrange
        when(userDetails.getUsername()).thenReturn(USER_EMAIL);
        final String token = jwtTokenService.generateJwtToken(userDetails, TokenType.ACCESS_TOKEN);

        // Act
        final Date expiration = jwtTokenService.extractExpiration(token);

        // Assert
        assertThat(expiration).isNotNull();
        final long expectedExpirationTime = System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION;
        // Allow for a small time window (1 second) for test execution
        assertThat(expiration.getTime()).isBetween(expectedExpirationTime - 1000, expectedExpirationTime + 1000);
    }

    @Test
    void extractClaim_shouldExtractSpecificClaim() {
        // Arrange
        when(userDetails.getUsername()).thenReturn(USER_EMAIL);
        final String token = jwtTokenService.generateJwtToken(userDetails, TokenType.ACCESS_TOKEN);

        // Act
        final String subject = jwtTokenService.extractClaim(token, Claims::getSubject);

        // Assert
        assertThat(subject).isEqualTo(USER_EMAIL);
    }

    // Helper method to access private method for testing
    private Claims extractAllClaims(final String token) {
        try {
            final Field secretKeyField = jwtTokenService.getClass().getDeclaredField("secretKey");
            secretKeyField.setAccessible(true);
            final String secretKey = (String) secretKeyField.get(jwtTokenService);

            return Jwts.parserBuilder()
                    .setSigningKey(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                            io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (final Exception e) {
            throw new RuntimeException("Error extracting claims", e);
        }
    }
}
