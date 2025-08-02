package com.feather.api.security.helpers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.feather.api.jpa.model.User;
import com.feather.api.security.tokens.AuthenticationRoles;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Factory class for building {@link FeatherAuthenticationToken} instances.
 */
@Component
public class AuthenticationTokenFactory {

    /**
     * Builds a {@link FeatherAuthenticationToken} for the given user, access token, and refresh token.
     * <p>
     * This method retrieves the current authentication context, constructs the credentials,
     * and combines the current authorities with the JWT role to create a new authentication token.
     * </p>
     *
     * @param user an authenticated user
     * @return a new {@link FeatherAuthenticationToken} containing the user, credentials, and authorities
     */
    public FeatherAuthenticationToken buildAuthenticationToken(final User user) {
        final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        final FeatherCredentials credentials = buildCredentials(user.getAccessToken(), user.getRefreshToken(), currentAuth);
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

    public FeatherAuthenticationToken buildAuthenticationTokenFromRequest(final String apiKey, final String accessToken, final String refreshToken,
            final User user) {
        final FeatherCredentials providedCredentials = new FeatherCredentials(apiKey, accessToken, refreshToken);
        return new FeatherAuthenticationToken(user, providedCredentials);
    }
}
