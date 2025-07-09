package com.feather.api.security.tokens;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Represents a JWT-based authentication token for Spring Security.
 * Stores the JWT and the authenticated user's details as principal.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;
    private final UserDetails principal;

    public JwtAuthenticationToken(String token) {
        super(null);
        this.token = token;
        this.principal= null;
        setAuthenticated(false);
    }

    public JwtAuthenticationToken(UserDetails principal, String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

}