package com.feather.api.security.provider;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;
import static com.feather.api.shared.TokenType.REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.jpa.service.UserService;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.security.tokens.AuthenticationRoles;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtTokenAuthenticationProviderTest {

    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private UserService userService;
    @Mock
    private FeatherAuthenticationToken authentication;
    @Mock
    private FeatherCredentials credentials;
    @Mock
    private User user;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Authentication currentAuthentication;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @InjectMocks
    private JwtTokenAuthenticationProvider classUnderTest;

    @BeforeEach
    void setUp() {
        securityContextHolderMock = mockStatic(SecurityContextHolder.class, Answers.RETURNS_DEEP_STUBS);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMock.close();
    }

    @Test
    void testAuthenticate_validAccessToken() {
        // Arrange
        final String accessToken = "valid-access-token";
        final String refreshToken = "valid-refresh-token";
        final String currentCreds = "current-creds";
        final SimpleGrantedAuthority apiKeyAuthority = new SimpleGrantedAuthority(AuthenticationRoles.WITH_API_KEY.name());
        final SimpleGrantedAuthority jwtTokenAuthority = new SimpleGrantedAuthority(AuthenticationRoles.WITH_JWT_TOKEN.name());
        final List<GrantedAuthority> value = new ArrayList<>(Collections.singleton(apiKeyAuthority));

        when(authentication.getCredentials()).thenReturn(credentials);
        when(authentication.getPrincipal()).thenReturn(user);
        when(credentials.accessToken()).thenReturn(accessToken);
        when(credentials.refreshToken()).thenReturn(refreshToken);
        when(jwtTokenService.isTokenExpired(accessToken)).thenReturn(false);
        when(jwtTokenService.isJwtTokenValid(accessToken, user, ACCESS_TOKEN)).thenReturn(true);

        securityContextHolderMock.when(() -> SecurityContextHolder.getContext().getAuthentication()).thenReturn(currentAuthentication);
        when(currentAuthentication.getCredentials()).thenReturn(currentCreds);
        doReturn(value).when(currentAuthentication).getAuthorities();

        // Act
        final Authentication result = classUnderTest.authenticate(authentication);

        // Assert
        assertThat(result).isInstanceOf(FeatherAuthenticationToken.class);
        final FeatherAuthenticationToken resultToken = (FeatherAuthenticationToken) result;
        assertThat(resultToken.getPrincipal()).isEqualTo(user);
        assertThat(resultToken.getAuthorities()).hasSize(2);
        assertThat(resultToken.getAuthorities()).contains(apiKeyAuthority);
        assertThat(resultToken.getAuthorities()).contains(jwtTokenAuthority);
    }

    @Test
    void testAuthenticate_expiredAccessToken_validRefreshToken() {
        // Arrange
        final String expiredAccessToken = "expired-access-token";
        final String validRefreshToken = "valid-refresh-token";
        final String currentCreds = "current-creds";
        final String newAccessToken = "new-access-token";

        when(authentication.getCredentials()).thenReturn(credentials);
        when(authentication.getPrincipal()).thenReturn(user);
        when(credentials.accessToken()).thenReturn(expiredAccessToken);
        when(credentials.refreshToken()).thenReturn(validRefreshToken);
        when(jwtTokenService.isTokenExpired(expiredAccessToken)).thenReturn(true);
        when(jwtTokenService.isTokenExpired(validRefreshToken)).thenReturn(false);
        when(jwtTokenService.isJwtTokenValid(validRefreshToken, user, REFRESH_TOKEN)).thenReturn(true);
        when(jwtTokenService.generateJwtToken(user, ACCESS_TOKEN)).thenReturn(newAccessToken);

        securityContextHolderMock.when(() -> SecurityContextHolder.getContext().getAuthentication()).thenReturn(currentAuthentication);
        when(currentAuthentication.getCredentials()).thenReturn(currentCreds);
        when(currentAuthentication.getAuthorities()).thenReturn(Set.of());

        // Act
        final Authentication result = classUnderTest.authenticate(authentication);

        // Assert
        assertThat(result).isInstanceOf(FeatherAuthenticationToken.class);
        verify(userService).updateUserToken(user, newAccessToken, REFRESH_TOKEN);
        verify(jwtTokenService).generateJwtToken(user, ACCESS_TOKEN);
    }

    @Test
    void testAuthenticate_expiredRefreshToken_throwsJwtAuthenticationException() {
        // Arrange
        final String expiredAccessToken = "expired-access-token";
        final String expiredRefreshToken = "expired-refresh-token";

        when(authentication.getCredentials()).thenReturn(credentials);
        when(authentication.getPrincipal()).thenReturn(user);
        when(credentials.accessToken()).thenReturn(expiredAccessToken);
        when(credentials.refreshToken()).thenReturn(expiredRefreshToken);
        when(jwtTokenService.isTokenExpired(expiredAccessToken)).thenReturn(true);
        when(jwtTokenService.isTokenExpired(expiredRefreshToken)).thenReturn(true);

        // Act & Assert
        final JwtAuthenticationException exception = assertThrows(
                JwtAuthenticationException.class,
                () -> classUnderTest.authenticate(authentication)
        );
        assertThat(exception.getMessage()).isEqualTo("Expired Refresh Token, log in again to get a new Refresh Token.");
    }

    @Test
    void testAuthenticate_invalidRefreshToken_throwsJwtAuthenticationException() {
        // Arrange
        final String expiredAccessToken = "expired-access-token";
        final String invalidRefreshToken = "invalid-refresh-token";

        when(authentication.getCredentials()).thenReturn(credentials);
        when(authentication.getPrincipal()).thenReturn(user);
        when(credentials.accessToken()).thenReturn(expiredAccessToken);
        when(credentials.refreshToken()).thenReturn(invalidRefreshToken);
        when(jwtTokenService.isTokenExpired(expiredAccessToken)).thenReturn(true);
        when(jwtTokenService.isTokenExpired(invalidRefreshToken)).thenReturn(false);
        when(jwtTokenService.isJwtTokenValid(invalidRefreshToken, user, REFRESH_TOKEN)).thenReturn(false);

        // Act & Assert
        final JwtAuthenticationException exception = assertThrows(
                JwtAuthenticationException.class,
                () -> classUnderTest.authenticate(authentication)
        );
        assertThat(exception.getMessage()).isEqualTo("Invalid Refresh Token");
    }

    @Test
    void testAuthenticate_invalidAccessToken_throwsJwtAuthenticationException() {
        // Arrange
        final String invalidAccessToken = "invalid-access-token";
        final String refreshToken = "refresh-token";

        when(authentication.getCredentials()).thenReturn(credentials);
        when(authentication.getPrincipal()).thenReturn(user);
        when(credentials.accessToken()).thenReturn(invalidAccessToken);
        when(credentials.refreshToken()).thenReturn(refreshToken);
        when(jwtTokenService.isTokenExpired(invalidAccessToken)).thenReturn(false);
        when(jwtTokenService.isJwtTokenValid(invalidAccessToken, user, ACCESS_TOKEN)).thenReturn(false);

        // Act & Assert
        final JwtAuthenticationException exception = assertThrows(
                JwtAuthenticationException.class,
                () -> classUnderTest.authenticate(authentication)
        );
        assertThat(exception.getMessage()).isEqualTo("Invalid Access Token");
    }

    @Test
    void testSupports_returnsTrueForFeatherAuthenticationToken() {
        assertThat(classUnderTest.supports(FeatherAuthenticationToken.class)).isTrue();
    }

    @Test
    void testSupports_returnsFalseForOtherToken() {
        assertThat(classUnderTest.supports(String.class)).isFalse();
    }
}