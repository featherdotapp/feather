package com.feather.api.security.tokens;

import java.util.Collection;

import com.feather.api.security.tokens.credentials.JwtTokenCredentials;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Represents a JWT-based authentication token for Spring Security.
 * Stores the JWT and the authenticated user's details as principal.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final JwtTokenCredentials credentials;
    private final UserDetails principal;

    public JwtAuthenticationToken(JwtTokenCredentials credentials) {
        super(null);
        this.credentials = credentials;
        this.principal= null;
        setAuthenticated(false);
    }

    public JwtAuthenticationToken(UserDetails principal, JwtTokenCredentials credentials, Collection<? extends GrantedAuthority> authorities) {
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