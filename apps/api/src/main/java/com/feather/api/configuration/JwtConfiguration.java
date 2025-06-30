package com.feather.api.configuration;

import com.feather.api.jpa.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuration class for JWT Tokens
 */
@Configuration
@RequiredArgsConstructor
public class JwtConfiguration {

    private final UserService userService;

    /**
     * Defines how to retrieve the user using the UserRepository that is injected
     *
     * @return an UserDetailService
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return userService::getUserFromEmail;
    }

    /**
     * Creates an instance of the BCryptPasswordEncoder used to encode the plain user password
     *
     * @return {@link BCryptPasswordEncoder}
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // TODO: is this needed?
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Creates a provider with the {@link JwtConfiguration#userDetailsService()} and the {@link JwtConfiguration#passwordEncoder()}
     *
     * @return a custom {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
