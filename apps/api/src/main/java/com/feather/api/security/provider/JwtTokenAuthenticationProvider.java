package com.feather.api.security.provider;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;
import static com.feather.api.shared.TokenType.REFRESH_TOKEN;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.jpa.service.UserService;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.security.tokens.AuthenticationRoles;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AuthenticationProvider implementation for JWT-based authentication.
 * Validates JWT tokens and loads user details for authentication.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenAuthenticationProvider implements AuthenticationProvider {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

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
        if (!jwtTokenService.isJwtTokenValid(accessToken, user, ACCESS_TOKEN)) {
            if (jwtTokenService.isTokenExpired(refreshToken)) {
                throw new JwtAuthenticationException("Expired Refresh Token, log in again to get a new Refresh Token.");
            }
            if (!jwtTokenService.isJwtTokenValid(refreshToken, user, REFRESH_TOKEN)) {
                throw new JwtAuthenticationException("Invalid Refresh Token");
            }
            final String newAccessToken = jwtTokenService.generateJwtToken(user, ACCESS_TOKEN);
            userService.updateUserToken(user, newAccessToken, ACCESS_TOKEN);
            return createAuthenticationToken(newAccessToken, refreshToken, user);
        }
        if (jwtTokenService.isTokenExpired(accessToken)) {
            throw new JwtAuthenticationException("Expired Access Token");
        }
        return createAuthenticationToken(accessToken, refreshToken, user);
    }

    private FeatherAuthenticationToken createAuthenticationToken(final String accessToken, final String refreshToken, final User user) {
        final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        final FeatherCredentials credentials = buildCredentials(accessToken, refreshToken, currentAuth);
        final Set<GrantedAuthority> authorities = buildAuthorities(currentAuth.getAuthorities());
        return new FeatherAuthenticationToken(user, credentials, authorities);
    }

    private FeatherCredentials buildCredentials(final String accessToken, final String refreshToken, final Authentication currentAuthentication) {
        final String currentAuthCredentials = (String) currentAuthentication.getCredentials();
        return new FeatherCredentials(currentAuthCredentials, accessToken, refreshToken);
    }

    private Set<GrantedAuthority> buildAuthorities(final Collection<? extends GrantedAuthority> currentAuthorities) {
        final SimpleGrantedAuthority jwtRole = new SimpleGrantedAuthority(AuthenticationRoles.WITH_JWT_TOKEN.name());
        final Set<GrantedAuthority> combinedAuthorities = new HashSet<>(currentAuthorities);
        combinedAuthorities.add(jwtRole);
        return combinedAuthorities;
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
