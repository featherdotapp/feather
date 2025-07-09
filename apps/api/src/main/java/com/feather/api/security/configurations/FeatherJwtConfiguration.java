package com.feather.api.security.configurations;

import com.feather.api.jpa.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuration class for JWT Tokens
 */
@Configuration
@RequiredArgsConstructor
public class FeatherJwtConfiguration {

    private final UserService userService;

    /**
     * Defines how to retrieve the user using the UserRepository that is injected
     *
     * @return userDetailService
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
}
