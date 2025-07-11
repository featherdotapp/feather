package com.feather.api.security.provider;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;
import static com.feather.api.shared.TokenType.REFRESH_TOKEN;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.jpa.service.UserService;
import com.feather.api.security.tokens.AuthenticationRoles;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.JwtAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import com.feather.api.security.tokens.credentials.JwtTokenCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        final JwtTokenCredentials jwtTokenCredentials = (JwtTokenCredentials) authentication.getCredentials();
        final String accessToken = jwtTokenCredentials.accessToken();
        final String refreshToken = jwtTokenCredentials.refreshToken();
        final User user = loadUserFromToken(accessToken);
        if (jwtTokenService.isTokenExpired(accessToken)) {
            if (jwtTokenService.isTokenExpired(refreshToken)) {
                throw new BadCredentialsException("Expired Refresh Token, log in again to get a new Refresh Token.");
            }
            if (!jwtTokenService.isJwtTokenValid(refreshToken, user, REFRESH_TOKEN)) {
                throw new BadCredentialsException("Invalid Refresh Token");
            }
            final String newAccessToken = jwtTokenService.generateJwtToken(user, ACCESS_TOKEN);
            userService.updateTokenById(user.getId(), newAccessToken, REFRESH_TOKEN);
            return createAuthenticationToken(newAccessToken, refreshToken, user);
        }
        if (!jwtTokenService.isJwtTokenValid(accessToken, user, ACCESS_TOKEN)) {
            throw new BadCredentialsException("Invalid Access Token");
        }
        return createAuthenticationToken(accessToken, refreshToken, user);
    }

    private FeatherAuthenticationToken createAuthenticationToken(final String accessToken, final String refreshToken, final User user) {
        final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        final FeatherCredentials credentials = buildCredentials(accessToken, refreshToken, currentAuth);
        final Set<GrantedAuthority> authorities = buildAuthorities(currentAuth.getAuthorities());
        return new FeatherAuthenticationToken(user, credentials, authorities);
    }

    private User loadUserFromToken(final String accessToken) throws UsernameNotFoundException {
        final String userName = jwtTokenService.extractUsername(accessToken);
        return userService.getUserFromEmail(userName);
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
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
