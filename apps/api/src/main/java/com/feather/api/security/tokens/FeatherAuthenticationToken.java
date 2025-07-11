package com.feather.api.security.tokens;

import java.util.Collection;

import com.feather.api.security.tokens.credentials.FeatherCredentials;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Authentication token for JWTToken-based authentication in Spring Security.
 * </br>
 * Stores the JWT Tokens (ACCESS & REFRESH) and the UserDetails where they are stored
 */
public class FeatherAuthenticationToken extends AbstractAuthenticationToken {

    private final FeatherCredentials credentials;
    private final UserDetails principal;

    public FeatherAuthenticationToken(final UserDetails principal, final FeatherCredentials credentials) {
        super(null);
        this.credentials = credentials;
        this.principal = principal;
        setAuthenticated(false);
    }

    public FeatherAuthenticationToken(final UserDetails principal, final FeatherCredentials credentials,
            final Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

}
