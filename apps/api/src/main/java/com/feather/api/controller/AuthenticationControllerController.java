package com.feather.api.controller;

import com.feather.api.adapter.linkedin.dto.LinkedInTokenResponse;
import com.feather.api.adapter.linkedin.dto.LinkedinUserInfoResponseDTO;
import com.feather.api.adapter.linkedin.service.LinkedinApiService;
import com.feather.api.jpa.model.Role;
import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.jpa.service.UserService;
import com.feather.api.security.oauth2.OAuth2Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final LinkedinApiService linkedinApiService;

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
     * TODO:
     *  build a check if an user with that email exists, handle case when refresh token gets invalid and the user has to reauthenticate, the data from
     *  (should the data form linkedin be updated?)
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
    public ResponseEntity<String> linkedinCallback(@RequestParam("code") String code) {
        final LinkedInTokenResponse tokenResponse = linkedinApiService.exchangeAuthorizationCodeForAccessToken(code);
        final String accessToken = tokenResponse.accessToken();
        final LinkedinUserInfoResponseDTO linkedinUserInfo = linkedinApiService.getMemberDetails(accessToken);
        final User user = createUserFromLinkedinProfile(linkedinUserInfo);
        final String jwtToken = generateAndAssignJwtToken(user);
        userService.saveUser(user);
        return ResponseEntity.ok(jwtToken);
    }

    private User createUserFromLinkedinProfile(LinkedinUserInfoResponseDTO linkedinUserInfo) {
        User user = new User();
        user.getOAuthProviders().add("LinkedIn");
        user.setEmail(linkedinUserInfo.email());
        user.setRole(Role.UNPAID_USER);
        return user;
    }

    private String generateAndAssignJwtToken(User user) {
        String jwtToken = jwtTokenService.generateAccessToken(user);
        user.setJwtToken(jwtToken);
        return jwtToken;
    }

    /**
     * TODO: possible endpoints
     *  - logout (make jwt tokens invalid)
     */

}
