package com.feather.api.security.providers;

import com.feather.api.jpa.model.User;
import com.feather.api.security.helpers.AuthenticationTokenFactory;
import com.feather.api.security.helpers.jwt.TokenRefresher;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * AuthenticationProvider implementation for JWT-based authentication.
 * Validates JWT tokens and loads user details for authentication.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenAuthenticationProvider implements AuthenticationProvider {

    private final AuthenticationTokenFactory authenticationTokenFactory;
    private final TokenRefresher tokenRefresher;

    /**
     * Authenticates the JWT token by validating it and loading the user.
     *
     * @param authentication the authentication request object
     * @return a fully authenticated object including credentials
     */
    @Override
    public Authentication authenticate(final Authentication authentication) {
        final FeatherCredentials credentials = (FeatherCredentials) authentication.getCredentials();
        final String accessToken = credentials.accessToken();
        final String refreshToken = credentials.refreshToken();
        final User user = (User) authentication.getPrincipal();
        final User updatedUser = tokenRefresher.refreshTokens(accessToken, refreshToken, user);
        return authenticationTokenFactory.buildAuthenticationToken(updatedUser);
    }

    /**
     * Checks if this provider supports the given authentication class.
     *
     * @param authentication the authentication class
     * @return true if supported, false otherwise
     */
    @Override
    public boolean supports(final Class<?> authentication) {
        return FeatherAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
