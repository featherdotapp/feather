package com.feather.api.service;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;
import static com.feather.api.shared.TokenType.REFRESH_TOKEN;

import com.feather.api.adapter.linkedin.dto.LinkedInTokenResponse;
import com.feather.api.adapter.linkedin.dto.LinkedinUserInfoResponseDTO;
import com.feather.api.adapter.linkedin.service.LinkedinApiService;
import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.UserService;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.JwtTokenCredentials;
import com.feather.api.service.jwt.JwtTokenBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service for handling authentication logic.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtTokenBuilder jwtTokenBuilder;
    private final LinkedinApiService linkedinApiService;
    private final UserService userService;

    /**
     * Registers a user using a LinkedIn authorization code and returns JWT credentials.
     *
     * @param code LinkedIn OAuth2 authorization code
     * @return JwtTokenCredentials containing access and refresh tokens
     */
    public JwtTokenCredentials register(final String code) {
        final LinkedInTokenResponse tokenResponse = linkedinApiService.exchangeAuthorizationCodeForAccessToken(code);
        final String accessToken = tokenResponse.accessToken();
        final LinkedinUserInfoResponseDTO linkedinUserInfo = linkedinApiService.getMemberDetails(accessToken);
        return handleUserAuthentication(linkedinUserInfo);
    }

    private JwtTokenCredentials handleUserAuthentication(final LinkedinUserInfoResponseDTO linkedinUserInfo) {
        try {
            final User existantUser = userService.getUserFromEmail(linkedinUserInfo.email());
            return createAndSaveJwtTokenCredentials(existantUser);
        } catch (final UsernameNotFoundException e) {
            return createUserAndGenerateJwtTokens(linkedinUserInfo);
        }
    }

    private JwtTokenCredentials createUserAndGenerateJwtTokens(final LinkedinUserInfoResponseDTO linkedinUserInfo) {
        final User user = new User();
        user.setEmail(linkedinUserInfo.email());
        return createAndSaveJwtTokenCredentials(user);
    }

    private JwtTokenCredentials createAndSaveJwtTokenCredentials(final User existantUser) {
        final String accessToken = jwtTokenBuilder.buildToken(existantUser, ACCESS_TOKEN);
        final String refreshToken = jwtTokenBuilder.buildToken(existantUser, REFRESH_TOKEN);
        existantUser.setAccessToken(accessToken);
        existantUser.setRefreshToken(refreshToken);
        userService.saveUser(existantUser);
        return new JwtTokenCredentials(accessToken, refreshToken);
    }

    /**
     * Logs out the currently authenticated user by invalidating their access and refresh tokens.
     * This method retrieves the authenticated user's credentials from the security context
     * and clears their tokens to terminate the session.
     *
     * @return true if the tokens were successfully cleared
     */
    public boolean logOut() {
        final FeatherAuthenticationToken authentication = (FeatherAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        final User user = (User) authentication.getPrincipal();
        final String emptyString = "";
        userService.updateUserToken(user, emptyString, ACCESS_TOKEN);
        userService.updateUserToken(user, emptyString, REFRESH_TOKEN);
        return true;
    }
}