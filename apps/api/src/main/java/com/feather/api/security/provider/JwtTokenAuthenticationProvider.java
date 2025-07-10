package com.feather.api.security.provider;

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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
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
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final String token = (String) authentication.getCredentials();
        final User user = loadUserFromToken(token);
        if (!jwtTokenService.isTokenValid(token, user)) {
            throw new BadCredentialsException("Invalid JWT Token");
        }
        final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        final FeatherCredentials credentials = buildCredentials(token, currentAuth);
        final Set<GrantedAuthority> authorities = buildAuthorities(currentAuth.getAuthorities());
        return new FeatherAuthenticationToken(user, credentials, authorities);
    }

    private User loadUserFromToken(String token) {
        final String userName = jwtTokenService.extractUsername(token);
        return userService.getUserFromEmail(userName);
    }

    private FeatherCredentials buildCredentials(String token, Authentication currentAuthentication) {
        final String currentAuthCredentials = (String) currentAuthentication.getCredentials();
        return new FeatherCredentials(token, currentAuthCredentials);
    }

    private Set<GrantedAuthority> buildAuthorities(Collection<? extends GrantedAuthority> currentAuthorities) {
        final SimpleGrantedAuthority jwtRole = new SimpleGrantedAuthority(AuthenticationRoles.WITH_JWT_TOKEN.name());
        Set<GrantedAuthority> combinedAuthorities = new HashSet<>(currentAuthorities);
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
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
