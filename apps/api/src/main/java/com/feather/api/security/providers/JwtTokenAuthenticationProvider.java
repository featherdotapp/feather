package com.feather.api.security.providers;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;

import java.util.Objects;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.UserService;
import com.feather.api.security.helpers.AuthenticationTokenFactory;
import com.feather.api.security.helpers.JwtTokenValidator;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * AuthenticationProvider implementation for JWT-based authentication.
 * Validates JWT tokens and loads user details for authentication.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenAuthenticationProvider implements AuthenticationProvider {

    private final JwtTokenValidator jwtTokenValidator;
    private final UserService userService;
    private final AuthenticationTokenFactory authenticationTokenFactory;

    /**
     * Authenticates the JWT token by validating it and loading the user.
     *
     * @param authentication the authentication request object
     * @return a fully authenticated object including credentials
     * @throws AuthenticationException if authentication fails
     */
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final FeatherCredentials credentials = (FeatherCredentials) authentication.getCredentials();
        final String accessToken = credentials.accessToken();
        final String refreshToken = credentials.refreshToken();
        final User user = (User) authentication.getPrincipal();
        final String validAccessToken = jwtTokenValidator.validateOrRefreshAccessToken(accessToken, refreshToken, user);
        if (!Objects.equals(validAccessToken, accessToken)) {
            userService.updateUserToken(user, validAccessToken, ACCESS_TOKEN);
        }
        return authenticationTokenFactory.buildAuthenticationToken(validAccessToken, refreshToken, user);
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
