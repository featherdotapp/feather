package com.feather.api.security.helpers.jwt;

import static com.feather.api.security.exception_handling.exception.JwtAuthenticationException.INVALID_ACCESS_TOKEN;
import static com.feather.api.security.exception_handling.exception.JwtAuthenticationException.INVALID_REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Date;

import com.feather.api.jpa.model.User;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.service.jwt.JwtTokenParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtTokenValidatorTest {

    private static final String SOME_EXPIRATION_DATE = "115200000";
    private static final Date VALID_EXPIRATION_DATE = new Date(System.currentTimeMillis() + 1000000);
    private static final Date INVALID_EXPIRATION_DATE = new Date(System.currentTimeMillis() - 1000000);
    private static final String VALID_ACCESS_TOKEN = "valid.access.token";
    private static final String VALID_REFRESH_TOKEN = "valid.refresh.token";
    private static final String EXPIRED_ACCESS_TOKEN = "expired.access.token";
    private static final String EXPIRED_REFRESH_TOKEN = "expired.refresh.token";
    private static final String USERNAME = "testuser";

    @Mock
    private JwtTokenParser jwtTokenParser;
    @Mock
    private User mockUser;
    @InjectMocks
    private JwtTokenValidator classUnderTest;

    @Test
    void shouldUpdateRefreshToken_isNotExpiringSoon() {
        // Arrange
        ReflectionTestUtils.setField(classUnderTest, "minRemainingTimeForRefreshRefreshToken", SOME_EXPIRATION_DATE);
        when(jwtTokenParser.extractExpirationDate(VALID_REFRESH_TOKEN)).thenReturn(new Date(System.currentTimeMillis() + 119200000L));

        // Act
        final boolean result = classUnderTest.shouldUpdateRefreshToken(VALID_REFRESH_TOKEN);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldUpdateRefreshToken_isExpiringSoon() {
        // Arrange
        ReflectionTestUtils.setField(classUnderTest, "minRemainingTimeForRefreshRefreshToken", SOME_EXPIRATION_DATE);
        when(jwtTokenParser.extractExpirationDate(VALID_REFRESH_TOKEN)).thenReturn(new Date(System.currentTimeMillis() + 1000L));

        // Act
        final boolean result = classUnderTest.shouldUpdateRefreshToken(VALID_REFRESH_TOKEN);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldUpdateRefreshToken_throwsException() {
        // Arrange
        ReflectionTestUtils.setField(classUnderTest, "minRemainingTimeForRefreshRefreshToken", SOME_EXPIRATION_DATE);
        when(jwtTokenParser.extractExpirationDate(VALID_REFRESH_TOKEN)).thenReturn(new Date(System.currentTimeMillis() - 10L));

        // Act
        assertThrows(JwtAuthenticationException.class, () -> classUnderTest.shouldUpdateRefreshToken(VALID_REFRESH_TOKEN));
    }

    @Test
    void shouldFailValidationWithInvalidRefreshToken() {
        // Arrange
        final String refreshToken = "invalid-refresh-token";
        when(mockUser.getUsername()).thenReturn(USERNAME);
        when(mockUser.getRefreshToken()).thenReturn("different-refresh-token");
        when(jwtTokenParser.extractSubject(refreshToken)).thenReturn(USERNAME);

        // Act & Assert - This should fail because tokens don't match
        assertThatThrownBy(() ->
                classUnderTest.shouldUpdateAccessToken("some-access-token", refreshToken, mockUser)
        )
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining(INVALID_REFRESH_TOKEN);
    }

    @Nested
    class UsernameNeeded {

        @BeforeEach
        void setUp() {
            when(mockUser.getUsername()).thenReturn(USERNAME);
        }

        @Test
        void validateOrRefreshAccessToken_WhenBothTokensValid_AndAccessTokenNotExpired_ShouldReturnSameAccessToken() {
            // Arrange
            setupValidTokens(VALID_ACCESS_TOKEN, VALID_REFRESH_TOKEN);
            when(jwtTokenParser.extractExpirationDate(VALID_ACCESS_TOKEN)).thenReturn(VALID_EXPIRATION_DATE);
            when(jwtTokenParser.extractExpirationDate(VALID_REFRESH_TOKEN)).thenReturn(VALID_EXPIRATION_DATE);

            // Act
            final boolean result = classUnderTest.shouldUpdateAccessToken(VALID_ACCESS_TOKEN, VALID_REFRESH_TOKEN, mockUser);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        void validateOrRefreshAccessToken_WhenAccessTokenExpired_AndRefreshTokenValid_ShouldGenerateNewAccessToken() {
            // Arrange
            setupValidTokens(EXPIRED_ACCESS_TOKEN, VALID_REFRESH_TOKEN);
            when(jwtTokenParser.extractExpirationDate(EXPIRED_ACCESS_TOKEN)).thenReturn(INVALID_EXPIRATION_DATE);
            when(jwtTokenParser.extractExpirationDate(VALID_REFRESH_TOKEN)).thenReturn(VALID_EXPIRATION_DATE);

            // Act
            final boolean result = classUnderTest.shouldUpdateAccessToken(EXPIRED_ACCESS_TOKEN, VALID_REFRESH_TOKEN, mockUser);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        void validateOrRefreshAccessToken_WhenRefreshTokenExpired_ShouldThrowException() {
            // Arrange
            setupValidTokens(EXPIRED_ACCESS_TOKEN, EXPIRED_REFRESH_TOKEN);
            when(jwtTokenParser.extractExpirationDate(EXPIRED_REFRESH_TOKEN)).thenReturn(INVALID_EXPIRATION_DATE);

            // Act & Assert
            assertThatThrownBy(() ->
                    classUnderTest.shouldUpdateAccessToken(EXPIRED_ACCESS_TOKEN, EXPIRED_REFRESH_TOKEN, mockUser)
            )
                    .isInstanceOf(JwtAuthenticationException.class)
                    .hasMessageContaining(JwtAuthenticationException.EXPIRED_REFRESH_TOKEN);
        }

        @Test
        void validateOrRefreshAccessToken_WhenRefreshTokenInvalid_ShouldThrowException() {
            // Arrange
            when(jwtTokenParser.extractSubject(VALID_REFRESH_TOKEN)).thenReturn("different_username");

            // Act & Assert
            assertThatThrownBy(() ->
                    classUnderTest.shouldUpdateAccessToken(VALID_ACCESS_TOKEN, VALID_REFRESH_TOKEN, mockUser)
            )
                    .isInstanceOf(JwtAuthenticationException.class)
                    .hasMessageContaining(INVALID_REFRESH_TOKEN);
        }

        @Test
        void validateOrRefreshAccessToken_WhenAccessTokenInvalid_ShouldThrowException() {
            // Arrange
            when(mockUser.getRefreshToken()).thenReturn(VALID_REFRESH_TOKEN);
            when(jwtTokenParser.extractSubject(VALID_REFRESH_TOKEN)).thenReturn(USERNAME);
            when(jwtTokenParser.extractSubject(VALID_ACCESS_TOKEN)).thenReturn("different_username");

            // Act & Assert
            assertThatThrownBy(() ->
                    classUnderTest.shouldUpdateAccessToken(VALID_ACCESS_TOKEN, VALID_REFRESH_TOKEN, mockUser)
            )
                    .isInstanceOf(JwtAuthenticationException.class)
                    .hasMessageContaining(INVALID_ACCESS_TOKEN);
        }

        private void setupValidTokens(final String accessToken, final String refreshToken) {
            when(mockUser.getAccessToken()).thenReturn(accessToken);
            when(mockUser.getRefreshToken()).thenReturn(refreshToken);
            when(jwtTokenParser.extractSubject(accessToken)).thenReturn(USERNAME);
            when(jwtTokenParser.extractSubject(refreshToken)).thenReturn(USERNAME);
        }
    }
}
