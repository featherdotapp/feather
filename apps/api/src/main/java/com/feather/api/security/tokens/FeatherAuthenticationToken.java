package com.feather.api.security.tokens;

import java.util.Collection;

import com.feather.api.security.tokens.credentials.FeatherCredentials;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class FeatherAuthenticationToken extends AbstractAuthenticationToken {

    private final FeatherCredentials credentials;
    private final UserDetails principal;

    public FeatherAuthenticationToken(UserDetails principal, FeatherCredentials credentials) {
        super(null);
        this.credentials = credentials;
        this.principal = principal;
        setAuthenticated(false);
    }

    public FeatherAuthenticationToken(UserDetails principal, FeatherCredentials credentials, Collection<? extends GrantedAuthority> authorities) {
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
