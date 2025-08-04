package com.feather.api.security.providers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feather.api.jpa.model.User;
import com.feather.api.security.helpers.AuthenticationTokenFactory;
import com.feather.api.security.helpers.jwt.TokenRefresher;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class JwtTokenAuthenticationProviderTest {

    @Mock
    private AuthenticationTokenFactory authenticationTokenFactory;
    @Mock
    private TokenRefresher tokenRefresher;
    @Mock
    private Authentication authentication;
    @Mock
    private FeatherCredentials credentials;
    @Mock
    private User user;
    @Mock
    private FeatherAuthenticationToken expectedToken;

    @InjectMocks
    private JwtTokenAuthenticationProvider classUnderTest;

    @Test
    void authenticate_shouldRefreshTokensAndReturnAuthenticationToken() {
        // Arrange
        final String accessToken = "access";
        final String refreshToken = "refresh";
        final User updatedUser = mock(User.class);
        when(authentication.getCredentials()).thenReturn(credentials);
        when(authentication.getPrincipal()).thenReturn(user);
        when(credentials.accessToken()).thenReturn(accessToken);
        when(credentials.refreshToken()).thenReturn(refreshToken);
        when(tokenRefresher.refreshTokens(accessToken, refreshToken, user)).thenReturn(updatedUser);
        when(authenticationTokenFactory.buildAuthenticationToken(updatedUser)).thenReturn(expectedToken);

        // Act
        final Authentication result = classUnderTest.authenticate(authentication);

        // Assert
        verify(tokenRefresher).refreshTokens(accessToken, refreshToken, user);
        verify(authenticationTokenFactory).buildAuthenticationToken(updatedUser);
        assertThat(result).isEqualTo(expectedToken);
    }

    @Test
    void supports_shouldReturnTrueForFeatherAuthenticationToken() {
        // Act
        final boolean result = classUnderTest.supports(FeatherAuthenticationToken.class);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void supports_shouldReturnFalseForOtherClass() {
        // Act
        final boolean result = classUnderTest.supports(String.class);

        // Assert
        assertThat(result).isFalse();
    }

}

