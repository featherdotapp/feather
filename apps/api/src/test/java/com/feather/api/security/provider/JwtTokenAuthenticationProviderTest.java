package com.feather.api.security.provider;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.jpa.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class JwtTokenAuthenticationProviderTest {

    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private UserService userService;
    @Mock
    private Authentication authentication;
    @Mock
    private User user;

    @InjectMocks
    private JwtTokenAuthenticationProvider classUnderTest;

    @Test
    void testAuthenticate_validToken_returnsAuthenticatedToken() {
        final String token = "valid-token";
        final String username = "user@example.com";
        when(authentication.getCredentials()).thenReturn(token);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.getUserFromEmail(username)).thenReturn(user);
        when(jwtTokenService.isJwtTokenValid(token, user, ACCESS_TOKEN)).thenReturn(true);

        final Authentication result = classUnderTest.authenticate(authentication);
        assertThat(result).isInstanceOf(com.feather.api.security.tokens.JwtAuthenticationToken.class);
        assertThat(result.isAuthenticated()).isTrue();
    }

    @Test
    void testAuthenticate_invalidToken_throwsBadCredentialsException() {
        final String token = "invalid-token";
        final String username = "user@example.com";
        when(authentication.getCredentials()).thenReturn(token);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.getUserFromEmail(username)).thenReturn(user);
        when(jwtTokenService.isJwtTokenValid(token, user, ACCESS_TOKEN)).thenReturn(false);

        Assertions.assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () ->
                classUnderTest.authenticate(authentication)
        );
    }

    @Test
    void testAuthenticate_nullUsername_throwsBadCredentialsException() {
        final String token = "token";
        when(authentication.getCredentials()).thenReturn(token);
        when(jwtTokenService.extractUsername(token)).thenReturn(null);

        Assertions.assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () ->
                classUnderTest.authenticate(authentication)
        );
    }

    @Test
    void testSupports_returnsTrueForJwtAuthenticationToken() {
        assertThat(classUnderTest.supports(com.feather.api.security.tokens.JwtAuthenticationToken.class)).isTrue();
    }

    @Test
    void testSupports_returnsFalseForOtherToken() {
        assertThat(classUnderTest.supports(String.class)).isFalse();
    }
}