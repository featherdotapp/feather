package com.feather.api.security.provider;

import java.util.List;

import com.feather.api.security.tokens.ApiKeyAuthenticationToken;
import com.feather.api.security.tokens.AuthenticationRoles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * AuthenticationProvider implementation for API key-based authentication.
 * Validates API keys and grants access if the key is valid.
 */
@Component
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

    @Value("${spring.valid-api-key}")
    private String validApiKey;

    /**
     * Authenticates the API key by validating it against the configured value.
     *
     * @param authentication the authentication request object
     * @return a fully authenticated object including credentials
     * @throws AuthenticationException if authentication fails
     */
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String apiKey = (String) authentication.getCredentials();
        if (validApiKey.equals(apiKey)) {
            return new ApiKeyAuthenticationToken(apiKey, List.of(new SimpleGrantedAuthority(AuthenticationRoles.WITH_API_KEY.name())));
        }
        throw new BadCredentialsException("Invalid API Key");
    }

    /**
     * Checks if this provider supports the given authentication class.
     *
     * @param authentication the authentication class
     * @return true if supported, false otherwise
     */
    @Override
    public boolean supports(final Class<?> authentication) {
        return ApiKeyAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
