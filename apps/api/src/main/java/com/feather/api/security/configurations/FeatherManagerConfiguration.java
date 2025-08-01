package com.feather.api.security.configurations;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

/**
 * Configuration class responsible for initializing Managers.
 */
@Configuration
@RequiredArgsConstructor
public class FeatherManagerConfiguration {

    private final List<AuthenticationProvider> authenticationProviders;

    /**
     * Authentication manager bean.
     *
     * @return the authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProviders);
    }
}
