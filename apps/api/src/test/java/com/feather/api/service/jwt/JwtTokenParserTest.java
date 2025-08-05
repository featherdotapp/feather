package com.feather.api.service.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtTokenParserTest {

    public static final String SECRET = "8f2b7e1c4a6d9e0b3c5f7a2e6d1c8b4f0e9a3d7c2b6f1e4a5c7d0b2f8a6e3c9";
    public static final String VALID_TOKEN =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZHJpYW4uYmF0aXN0YWJlcmluZ2VyQGdtYWlsLmNvbSIsImlhdCI6MTc1MzQ0MjU5NiwiZXhwIjoxNzU0NjUyMTk2fQ.McOEaQdmDAxkUg6w4chp9R0rwHQCyoqF2BumplTlUwk";
    @Mock
    private JwtTokenBuilder jwtTokenBuilder;

    @InjectMocks
    private JwtTokenParser classUnderTest;

    @Test
    void testExtractExpiration() {
        // Arrange
        mockSecret();

        // Act
        final Date date = classUnderTest.extractExpirationDate(VALID_TOKEN);

        // Assert
        assertThat(date).isNotNull();
    }

    @Test
    void testExtractExpiration_exceptionIsThrownButExpirationIsReturned() {
        // Arrange
        final String expiredToken =
                "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJERUZBVUxUX1VTRVIiXSwic3ViIjoiYWRyaWFuLmJhdGlzdGFiZXJpbmdlckBnbWFpbC5jb20iLCJpYXQiOjE3NTM3NzM0NjYsImV4cCI6MTc1Mzc3NTI2Nn0.WBedv1DbBrJd-bTmFTG7v_Ut3lQjjNjENhuO09I8kB8";
        mockSecret();

        // Act
        final Date date = classUnderTest.extractExpirationDate(expiredToken);

        // Assert
        assertThat(date).isNotNull();
    }

    @Test
    void testExtractSubject() {
        // Arrange
        mockSecret();

        // Act
        final String user = classUnderTest.extractSubject(VALID_TOKEN);

        // Assert
        assertThat(user).endsWith("@gmail.com").isNotNull();
    }

    @Test
    void testExtractSubject_throwsExceptionButReturnsSubject() {
        // Arrange
        final String expiredToken =
                "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJERUZBVUxUX1VTRVIiXSwic3ViIjoiYWRyaWFuLmJhdGlzdGFiZXJpbmdlckBnbWFpbC5jb20iLCJpYXQiOjE3NTM3NzM0NjYsImV4cCI6MTc1Mzc3NTI2Nn0.WBedv1DbBrJd-bTmFTG7v_Ut3lQjjNjENhuO09I8kB8";
        mockSecret();

        // Act
        final String user = classUnderTest.extractSubject(expiredToken);

        // Assert
        assertThat(user).endsWith("@gmail.com").isNotNull();
    }

    private void mockSecret() {
        final byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        final SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        when(jwtTokenBuilder.getSignInKey()).thenReturn(secretKey);
    }

}