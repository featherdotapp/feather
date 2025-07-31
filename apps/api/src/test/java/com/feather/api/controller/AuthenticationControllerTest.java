package com.feather.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.feather.api.security.oauth2.OAuth2Provider;
import com.feather.api.security.tokens.credentials.JwtTokenCredentials;
import com.feather.api.service.AuthenticationService;
import com.feather.api.service.RedirectService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private static final String CLIENT_ID = "test-client-id";
    private static final String REDIRECT_URI = "http://localhost:3000/callback";
    private static final String SCOPE = "r_liteprofile%20r_emailaddress";
    private static final String AUTH_CODE = "test-auth-code";
    private static final String EXPECTED_LOGIN_URL = "https://www.linkedin.com/oauth/v2/authorization" +
            "?response_type=code" +
            "&client_id=" + CLIENT_ID +
            "&redirect_uri=" + REDIRECT_URI +
            "&scope=" + SCOPE;

    @Mock
    private OAuth2Provider oAuth2Provider;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private RedirectService redirectService;
    @Mock
    private HttpServletResponse response;
    @Mock
    private JwtTokenCredentials jwtTokenCredentials;

    @InjectMocks
    private AuthenticationController classUnderTest;

    @Test
    void linkedinLoginUrl_shouldGenerateCorrectUrl() {
        // Arrange
        when(oAuth2Provider.getLinkedinClientId()).thenReturn(CLIENT_ID);
        when(oAuth2Provider.getLinkedinRedirectUri()).thenReturn(REDIRECT_URI);
        when(oAuth2Provider.getLinkedinScope()).thenReturn(SCOPE);

        // Act
        final String result = classUnderTest.linkedinLoginUrl();

        // Assert
        assertThat(result).isEqualTo(EXPECTED_LOGIN_URL);
    }

    @Test
    void linkedinCallback_shouldRegisterUserAndRedirect() throws IOException {
        // Arrange
        when(authenticationService.register(AUTH_CODE)).thenReturn(jwtTokenCredentials);

        // Act
        classUnderTest.linkedinCallback(AUTH_CODE, response);

        // Assert
        verify(authenticationService).register(AUTH_CODE);
        verify(redirectService).registerRedirect(response, jwtTokenCredentials);
    }

    @Test
    void isAuthenticated_shouldReturnOkResponse() {
        // Act
        final ResponseEntity<Boolean> result = classUnderTest.isAuthenticated();

        // Assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isTrue();
    }

    @Test
    void logout_shouldLogOutUserAndReturnResult() {
        // Arrange
        when(authenticationService.logOut()).thenReturn(true);

        // Act
        final ResponseEntity<Boolean> result = classUnderTest.logout();

        // Assert
        verify(authenticationService).logOut();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isTrue();
    }

    @Test
    void logout_shouldHandleFailedLogout() {
        // Arrange
        when(authenticationService.logOut()).thenReturn(false);

        // Act
        final ResponseEntity<Boolean> result = classUnderTest.logout();

        // Assert
        verify(authenticationService).logOut();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isFalse();
    }
}
