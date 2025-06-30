package com.feather.api.configuration;

import com.feather.api.configuration.filters.ApiKeyAuthenticationFilter;
import com.feather.api.configuration.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * Custom Spring security configuration class for the Feather API
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class FeatherSecurityConfiguration {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    /**
     * TODO:
     *  - Exception handling
     *  - Check if CSRF protection is needed (own API-Key + JWT)
     *
     * Configures the security filter chain to:
     * <ul>
     *   <li>Require a valid API key for all endpoints, including /auth/login and /auth/signup.</li>
     *   <li>Require both API key and JWT authentication for all endpoints except /auth/login and /auth/signup.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if a configuration error occurs
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsFilter corsFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(apiKeyAuthenticationFilter, CorsFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, ApiKeyAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/signup").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider);
        return http.build();
    }

}
