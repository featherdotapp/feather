package com.feather.api.security.tokens;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Authentication token for API key-based authentication in Spring Security.
 * Stores the API key and associated authorities.
 */
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String apiKey;

    /**
     * Constructor for ApiKeyAuthenticationToken with an empty authority list
     *
     * @param apiKey the api key authentication
     */
    public ApiKeyAuthenticationToken(final String apiKey) {
        super(null);
        this.apiKey = apiKey;
        setAuthenticated(false);
    }

    /**
     * Constructor for the ApiKeyAuthenticationToken
     *
     * @param apiKey the api key authentication
     * @param authorities authorities for the Authentication
     */
    public ApiKeyAuthenticationToken(final String apiKey, final Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.apiKey = apiKey;
        setAuthenticated(true);
    }

    @Override
    public String getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return "apiKey";
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
