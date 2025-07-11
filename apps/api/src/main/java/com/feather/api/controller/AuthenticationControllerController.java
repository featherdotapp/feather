package com.feather.api.controller;

import com.feather.api.security.oauth2.OAuth2Provider;
import com.feather.api.security.tokens.credentials.JwtTokenCredentials;
import com.feather.api.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller handling authentication-related endpoints.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationControllerController {

    private final OAuth2Provider oAuth2Provider;
    private final AuthenticationService authenticationService;

    /**
     * Generates the LinkedIn OAuth2 authorization URL.
     * Users should be redirected to this URL to initiate the OAuth2 flow.
     *
     * @return String containing the complete LinkedIn authorization URL with all required parameters
     */
    @GetMapping("/linkedin/loginUrl")
    public String linkedinLoginUrl() {
        return "https://www.linkedin.com/oauth/v2/authorization" +
                "?response_type=code" +
                "&client_id=" + oAuth2Provider.getLinkedinClientId() +
                "&redirect_uri=" + oAuth2Provider.getLinkedinRedirectUri() +
                "&scope=" + oAuth2Provider.getLinkedinScope();
    }

    /**
     * Handles the OAuth2 callback from LinkedIn after successful authorization.
     * This endpoint:
     * 1. Exchanges the authorization code for access token
     * 2. Retrieves user information from LinkedIn
     * 3. Creates or updates the user in our system
     * 4. Generates a JWT token for later authentication
     *
     * @param code The authorization code provided by LinkedIn's OAuth2 service
     * @return ResponseEntity<User> containing the user information and JWT token
     */
    @GetMapping("/linkedin/callback")
    public ResponseEntity<String> linkedinCallback(@RequestParam("code") final String code) {
        final JwtTokenCredentials tokens = authenticationService.register(code);
        return ResponseEntity.ok(tokens.accessToken());
    }

    /**
     * Logs out the currently authenticated user by invalidating their JWT tokens.
     * This method clears the access and refreshes tokens associated with the user.
     *
     * @return ResponseEntity<Boolean> indicating whether the logout operation was successful
     */
    @PostMapping("/logout")
    public ResponseEntity<Boolean> login() {
        final boolean loggedOut = authenticationService.logOut();
        return ResponseEntity.ok(loggedOut);
    }

}
