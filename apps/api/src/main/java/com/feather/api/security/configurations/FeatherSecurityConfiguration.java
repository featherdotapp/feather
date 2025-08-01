package com.feather.api.security.configurations;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAuthority;
import static org.springframework.security.authorization.AuthorizationManagers.allOf;

import com.feather.api.security.configurations.model.EndpointPaths;
import com.feather.api.security.exception_handling.FeatherAuthenticationEntryPoint;
import com.feather.api.security.filters.ApiKeyFilter;
import com.feather.api.security.filters.JwtTokenFilter;
import com.feather.api.security.tokens.AuthenticationRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration for the Feather API.
 * Defines security filter chains for API key, JWT, and public endpoints.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class FeatherSecurityConfiguration {

    private final ApiKeyFilter apiKeyFilter;
    private final JwtTokenFilter jwtTokenFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final FeatherAuthenticationEntryPoint authenticationEntryPoint;
    private final EndpointPaths endpointPaths;

    /**
     * Security filter chain for public endpoints (no authentication required).
     *
     * @param http the HttpSecurity to modify
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain publicChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // disabled because of custom security
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .securityMatcher(endpointPaths.unauthenticatedPaths())
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
                        httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(authenticationEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * Security filter chain for endpoints secured with an API key.
     *
     * @param http the HttpSecurity to modify
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain apiKeyChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // disabled because of custom security
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .securityMatcher(endpointPaths.apiKeyAuthenticatedPaths())
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
                        httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(authenticationEntryPoint)
                )
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().hasAuthority(AuthenticationRoles.WITH_API_KEY.name()));
        return http.build();
    }

    /**
     * Security filter chain for endpoints secured with both API keys and JWT tokens.
     *
     * @param http the HttpSecurity to modify
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain fullyAuthenticatedChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // disabled because of custom security
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .securityMatcher(endpointPaths.fullyAuthenticatedPaths())
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
                        httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(authenticationEntryPoint)
                )
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtTokenFilter, ApiKeyFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest()
                        .access(allOf(
                                hasAuthority(AuthenticationRoles.WITH_API_KEY.name()),
                                hasAuthority(AuthenticationRoles.WITH_JWT_TOKEN.name()))
                        )
                );
        return http.build();
    }

}
