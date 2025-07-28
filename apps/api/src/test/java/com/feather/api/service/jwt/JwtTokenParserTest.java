package com.feather.api.service.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtTokenParserTest {

    @Mock
    private JwtTokenBuilder jwtTokenBuilder;

    @InjectMocks
    private JwtTokenParser classUnderTest;

    @Test
    void testExtractClaim_extractExpiration() {
        // Arrange
        final String token =
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZHJpYW4uYmF0aXN0YWJlcmluZ2VyQGdtYWlsLmNvbSIsImlhdCI6MTc1MzQ0MjU5NiwiZXhwIjoxNzU0NjUyMTk2fQ.McOEaQdmDAxkUg6w4chp9R0rwHQCyoqF2BumplTlUwk";
        final byte[] keyBytes = Decoders.BASE64.decode("8f2b7e1c4a6d9e0b3c5f7a2e6d1c8b4f0e9a3d7c2b6f1e4a5c7d0b2f8a6e3c9");
        final SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        when(jwtTokenBuilder.getSignInKey()).thenReturn(secretKey);

        // Act
        final Date date = classUnderTest.extractClaim(token, Claims::getExpiration);

        // Assert
        assertThat(date).isNotNull();
    }

    @Test
    void testExtractClaim_extractSubject() {
        // Arrange
        final String token =
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZHJpYW4uYmF0aXN0YWJlcmluZ2VyQGdtYWlsLmNvbSIsImlhdCI6MTc1MzQ0MjU5NiwiZXhwIjoxNzU0NjUyMTk2fQ.McOEaQdmDAxkUg6w4chp9R0rwHQCyoqF2BumplTlUwk";
        final byte[] keyBytes = Decoders.BASE64.decode("8f2b7e1c4a6d9e0b3c5f7a2e6d1c8b4f0e9a3d7c2b6f1e4a5c7d0b2f8a6e3c9");
        final SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        when(jwtTokenBuilder.getSignInKey()).thenReturn(secretKey);

        // Act
        final String username = classUnderTest.extractClaim(token, Claims::getSubject);

        // Assert
        assertThat(username).endsWith("@gmail.com").isNotNull();
    }

}