package com.feather.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feather.api.adapter.linkedin.dto.LinkedInTokenResponse;
import com.feather.api.adapter.linkedin.dto.LinkedinUserInfoResponseDTO;
import com.feather.api.adapter.linkedin.service.LinkedinApiService;
import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.UserService;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.JwtTokenCredentials;
import com.feather.api.service.jwt.JwtTokenBuilder;
import com.feather.api.shared.TokenType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    public static final String CODE = "code";
    public static final String SOME_ACCESS_TOKEN = "someAccessToken";
    public static final String USER_EMAIL = "newuser@email.com";
    public static final String ACCESS_TOKEN = "access-token";
    public static final String REFRESH_TOKEN = "refresh-token";
    @Mock
    private FeatherAuthenticationToken authentication;
    @Mock
    private User principal;
    @Mock
    private UserService userService;
    @Mock
    private JwtTokenBuilder jwtTokenBuilder;
    @Mock
    private LinkedinApiService linkedinApiService;
    @Mock
    private LinkedInTokenResponse tokenResponse;
    @Mock
    private LinkedinUserInfoResponseDTO linkedinUserInfo;
    @Mock
    private User user;

    private MockedStatic<SecurityContextHolder> secuirtyContextHolderMockedStatic;

    @InjectMocks
    private AuthenticationService classUnderTest;

    @BeforeEach
    void setUp() {
        secuirtyContextHolderMockedStatic = mockStatic(SecurityContextHolder.class, RETURNS_DEEP_STUBS);
    }

    @AfterEach
    void tearDown() {
        secuirtyContextHolderMockedStatic.close();
    }

    @Test
    void testRegister() {
        // Arrange

        when(linkedinApiService.exchangeAuthorizationCodeForAccessToken(CODE)).thenReturn(tokenResponse);
        when(tokenResponse.accessToken()).thenReturn(SOME_ACCESS_TOKEN);
        when(linkedinApiService.getMemberDetails(SOME_ACCESS_TOKEN)).thenReturn(linkedinUserInfo);
        when(linkedinUserInfo.email()).thenReturn(USER_EMAIL);
        when(userService.getUserFromEmail(USER_EMAIL)).thenReturn(user);
        when(jwtTokenBuilder.buildToken(user, TokenType.ACCESS_TOKEN)).thenReturn(ACCESS_TOKEN);
        when(jwtTokenBuilder.buildToken(user, TokenType.REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN);

        // Act
        final JwtTokenCredentials register = classUnderTest.register(CODE);

        // Assert
        verify(user).setAccessToken(ACCESS_TOKEN);
        verify(user).setRefreshToken(REFRESH_TOKEN);
        verify(userService).saveUser(user);
        assertThat(register.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(register.refreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    void testRegister_newUserCreation() {
        // Arrange
        when(linkedinApiService.exchangeAuthorizationCodeForAccessToken(CODE)).thenReturn(tokenResponse);
        when(tokenResponse.accessToken()).thenReturn(SOME_ACCESS_TOKEN);
        when(linkedinApiService.getMemberDetails(SOME_ACCESS_TOKEN)).thenReturn(linkedinUserInfo);
        when(linkedinUserInfo.email()).thenReturn(USER_EMAIL);
        when(userService.getUserFromEmail(USER_EMAIL)).thenThrow(new UsernameNotFoundException("User not found"));
        when(jwtTokenBuilder.buildToken(any(User.class), eq(TokenType.ACCESS_TOKEN))).thenReturn(ACCESS_TOKEN);
        when(jwtTokenBuilder.buildToken(any(User.class), eq(TokenType.REFRESH_TOKEN))).thenReturn(REFRESH_TOKEN);

        // Act
        final JwtTokenCredentials register = classUnderTest.register(CODE);

        // Assert
        verify(userService).saveUser(any(User.class));
        assertThat(register.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(register.refreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    void testLogOut() {
        // Arrange
        secuirtyContextHolderMockedStatic.when(() -> SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);

        // Act
        classUnderTest.logOut();

        // Assert
        verify(userService).updateUserToken(principal, "", TokenType.ACCESS_TOKEN);
        verify(userService).updateUserToken(principal, "", TokenType.REFRESH_TOKEN);

    }
}