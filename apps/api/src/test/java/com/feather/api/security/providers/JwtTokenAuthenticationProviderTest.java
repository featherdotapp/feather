package com.feather.api.security.providers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.UserService;
import com.feather.api.security.helpers.AuthenticationTokenFactory;
import com.feather.api.security.helpers.JwtTokenValidator;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class JwtTokenAuthenticationProviderTest {

    private static final String ACCESS_TOKEN = "test.access.token";
    private static final String REFRESH_TOKEN = "test.refresh.token";
    private static final String NEW_ACCESS_TOKEN = "new.access.token";

    @Mock
    private JwtTokenValidator jwtTokenValidator;
    @Mock
    private UserService userService;
    @Mock
    private AuthenticationTokenFactory authenticationTokenFactory;
    @Mock
    private FeatherAuthenticationToken mockAuthentication;
    @Mock
    private User mockUser;

    @InjectMocks
    private JwtTokenAuthenticationProvider classUnderTest;

    @Test
    void supports_WithFeatherAuthenticationToken_ShouldReturnTrue() {
        // Act
        final boolean result = classUnderTest.supports(FeatherAuthenticationToken.class);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void supports_WithDifferentAuthenticationClass_ShouldReturnFalse() {
        // Act
        final boolean result = classUnderTest.supports(UsernamePasswordAuthenticationToken.class);

        // Assert
        assertThat(result).isFalse();
    }

    @Nested
    class Authenticate {

        @BeforeEach
        void setUp() {
            final FeatherCredentials credentials = new FeatherCredentials("apiKey", ACCESS_TOKEN, REFRESH_TOKEN);
            when(mockAuthentication.getCredentials()).thenReturn(credentials);
            when(mockAuthentication.getPrincipal()).thenReturn(mockUser);
        }

        @Test
        void authenticate_WhenAccessTokenValid_ShouldReturnAuthenticationWithSameToken() {
            // Arrange
            when(jwtTokenValidator.validateOrRefreshAccessToken(ACCESS_TOKEN, REFRESH_TOKEN, mockUser))
                    .thenReturn(ACCESS_TOKEN);
            when(authenticationTokenFactory.buildAuthenticationToken(ACCESS_TOKEN, REFRESH_TOKEN, mockUser))
                    .thenReturn(mockAuthentication);

            // Act
            final Authentication result = classUnderTest.authenticate(mockAuthentication);

            // Assert
            assertThat(result).isEqualTo(mockAuthentication);
            verify(userService, never()).updateUserToken(any(), any(), any());
        }

        @Test
        void authenticate_WhenAccessTokenRefreshed_ShouldUpdateUserAndReturnNewToken() {
            // Arrange
            when(jwtTokenValidator.validateOrRefreshAccessToken(ACCESS_TOKEN, REFRESH_TOKEN, mockUser))
                    .thenReturn(NEW_ACCESS_TOKEN);
            when(authenticationTokenFactory.buildAuthenticationToken(NEW_ACCESS_TOKEN, REFRESH_TOKEN, mockUser))
                    .thenReturn(mockAuthentication);

            // Act
            final Authentication result = classUnderTest.authenticate(mockAuthentication);

            // Assert
            assertThat(result).isEqualTo(mockAuthentication);
            verify(userService).updateUserToken(mockUser, NEW_ACCESS_TOKEN, com.feather.api.shared.TokenType.ACCESS_TOKEN);
        }
    }

}