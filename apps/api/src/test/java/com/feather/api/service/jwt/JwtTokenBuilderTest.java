package com.feather.api.service.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Key;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtTokenBuilderTest {

    private static final String SECRET_KEY = "8f2b7e1c4a6d9e0b3c5f7a2e6d1c8b4f0e9a3d7c2b6f1e4a5c7d0b2f8a6e3c9";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 1 week

    @Spy
    @InjectMocks
    private JwtTokenBuilder classUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(classUnderTest, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(classUnderTest, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(classUnderTest, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
    }

    @Test
    void buildToken_withAccessToken_shouldGenerateValidTokenWithRoles() {
        // Arrange
        final UserDetails userDetails = new User("testuser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // Act
        final String token = classUnderTest.buildToken(userDetails, TokenType.ACCESS_TOKEN);

        // Assert
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(classUnderTest.getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("roles", List.class)).contains("ROLE_USER");
        assertThat(claims.getExpiration()).isCloseTo(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION), 1300);
    }

    @Test
    void buildToken_withRefreshToken_shouldGenerateValidTokenWithoutRoles() {
        // Arrange
        final UserDetails userDetails = new User("testuser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // Act
        final String token = classUnderTest.buildToken(userDetails, TokenType.REFRESH_TOKEN);

        // Assert
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(classUnderTest.getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("roles")).isNull();
        assertThat(claims.getExpiration()).isCloseTo(
                new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION),
                1000
        );
    }

    @Test
    void getSignInKey_shouldReturnValidKey() {
        // Act
        final Key key = classUnderTest.getSignInKey();

        // Assert
        assertThat(key).isNotNull();
        assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");
    }

    @Test
    void getAccessTokenExpiration_shouldReturnConfiguredValue() {
        // Assert
        assertThat(classUnderTest.getAccessTokenExpiration()).isEqualTo(ACCESS_TOKEN_EXPIRATION);
    }

    @Test
    void getRefreshTokenExpiration_shouldReturnConfiguredValue() {
        // Assert
        assertThat(classUnderTest.getRefreshTokenExpiration()).isEqualTo(REFRESH_TOKEN_EXPIRATION);
    }
}