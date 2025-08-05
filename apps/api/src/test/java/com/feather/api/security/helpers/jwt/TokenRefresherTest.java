package com.feather.api.security.helpers.jwt;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;
import static com.feather.api.shared.TokenType.REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.UserService;
import com.feather.api.service.jwt.JwtTokenBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenRefresherTest {

    private static final String ACCESS_TOKEN_VALUE = "access";
    private static final String REFRESH_TOKEN_VALUE = "refresh";
    private static final String NEW_ACCESS_TOKEN = "newAccess";
    private static final String MAIL = "user@email.com";
    private static final String NEW_REFRESH_TOKEN = "newRefresh";

    @Mock
    private UserService userService;
    @Mock
    private JwtTokenValidator jwtTokenValidator;
    @Mock
    private JwtTokenBuilder jwtTokenBuilder;

    @InjectMocks
    private TokenRefresher classUnderTest;

    @Mock
    private User user;

    @Test
    void refreshTokens_onlyAccessTokenRefreshed() {
        // Arrange
        when(jwtTokenValidator.shouldUpdateAccessToken(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user)).thenReturn(true);
        when(jwtTokenValidator.shouldUpdateRefreshToken(REFRESH_TOKEN_VALUE)).thenReturn(false);
        when(jwtTokenBuilder.buildToken(user, ACCESS_TOKEN)).thenReturn(NEW_ACCESS_TOKEN);
        when(user.getEmail()).thenReturn(MAIL);
        final User updatedUser = mock(User.class);
        when(userService.getUserFromEmail(MAIL)).thenReturn(updatedUser);

        // Act
        final User result = classUnderTest.refreshTokens(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user);

        // Assert
        verify(jwtTokenBuilder).buildToken(user, ACCESS_TOKEN);
        verify(userService).updateUserToken(user, NEW_ACCESS_TOKEN, ACCESS_TOKEN);
        verify(userService).getUserFromEmail(MAIL);
        verify(jwtTokenValidator).shouldUpdateAccessToken(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user);
        verify(jwtTokenValidator).shouldUpdateRefreshToken(REFRESH_TOKEN_VALUE);
        verifyNoMoreInteractions(userService, jwtTokenBuilder);
        assertThat(result).isEqualTo(updatedUser);
    }

    @Test
    void refreshTokens_onlyRefreshTokenRefreshed() {
        // Arrange
        when(jwtTokenValidator.shouldUpdateAccessToken(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user)).thenReturn(false);
        when(jwtTokenValidator.shouldUpdateRefreshToken(REFRESH_TOKEN_VALUE)).thenReturn(true);
        when(jwtTokenBuilder.buildToken(user, REFRESH_TOKEN)).thenReturn(NEW_REFRESH_TOKEN);
        when(user.getEmail()).thenReturn(MAIL);
        final User updatedUser = mock(User.class);
        when(userService.getUserFromEmail(MAIL)).thenReturn(updatedUser);

        // Act
        final User result = classUnderTest.refreshTokens(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user);

        // Assert
        verify(jwtTokenBuilder).buildToken(user, REFRESH_TOKEN);
        verify(userService).updateUserToken(user, NEW_REFRESH_TOKEN, REFRESH_TOKEN);
        verify(userService).getUserFromEmail(MAIL);
        verify(jwtTokenValidator).shouldUpdateAccessToken(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user);
        verify(jwtTokenValidator).shouldUpdateRefreshToken(REFRESH_TOKEN_VALUE);
        verifyNoMoreInteractions(userService, jwtTokenBuilder);
        assertThat(result).isEqualTo(updatedUser);
    }

    @Test
    void refreshTokens_bothTokensRefreshed() {
        // Arrange
        when(jwtTokenValidator.shouldUpdateAccessToken(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user)).thenReturn(true);
        when(jwtTokenValidator.shouldUpdateRefreshToken(REFRESH_TOKEN_VALUE)).thenReturn(true);
        when(jwtTokenBuilder.buildToken(user, ACCESS_TOKEN)).thenReturn(NEW_ACCESS_TOKEN);
        when(jwtTokenBuilder.buildToken(user, REFRESH_TOKEN)).thenReturn(NEW_REFRESH_TOKEN);
        when(user.getEmail()).thenReturn(MAIL);
        final User updatedUser = mock(User.class);
        when(userService.getUserFromEmail(MAIL)).thenReturn(updatedUser);

        // Act
        final User result = classUnderTest.refreshTokens(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user);

        // Assert
        verify(jwtTokenBuilder).buildToken(user, ACCESS_TOKEN);
        verify(jwtTokenBuilder).buildToken(user, REFRESH_TOKEN);
        verify(userService).updateUserToken(user, NEW_ACCESS_TOKEN, ACCESS_TOKEN);
        verify(userService).updateUserToken(user, NEW_REFRESH_TOKEN, REFRESH_TOKEN);
        verify(userService).getUserFromEmail(MAIL);
        verify(jwtTokenValidator).shouldUpdateAccessToken(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user);
        verify(jwtTokenValidator).shouldUpdateRefreshToken(REFRESH_TOKEN_VALUE);
        verifyNoMoreInteractions(userService, jwtTokenBuilder);
        assertThat(result).isEqualTo(updatedUser);
    }

    @Test
    void refreshTokens_noTokensRefreshed() {
        // Arrange
        when(jwtTokenValidator.shouldUpdateAccessToken(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user)).thenReturn(false);
        when(jwtTokenValidator.shouldUpdateRefreshToken(REFRESH_TOKEN_VALUE)).thenReturn(false);
        when(user.getEmail()).thenReturn(MAIL);
        final User updatedUser = mock(User.class);
        when(userService.getUserFromEmail(MAIL)).thenReturn(updatedUser);

        // Act
        final User result = classUnderTest.refreshTokens(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user);

        // Assert
        verify(userService).getUserFromEmail(MAIL);
        verify(jwtTokenValidator).shouldUpdateAccessToken(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE, user);
        verify(jwtTokenValidator).shouldUpdateRefreshToken(REFRESH_TOKEN_VALUE);
        verifyNoMoreInteractions(userService, jwtTokenBuilder);
        assertThat(result).isEqualTo(updatedUser);
    }
}