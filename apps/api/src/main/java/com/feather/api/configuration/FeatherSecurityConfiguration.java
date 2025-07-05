package com.feather.api.configuration;

import com.feather.api.configuration.access_managers.ApiKeyOnlyAccessManager;
import com.feather.api.configuration.access_managers.JwtAndApiKeyAccessManager;
import com.feather.api.configuration.constants.RequestMatchers;
import com.feather.api.configuration.filters.ApiKeyFilter;
import com.feather.api.configuration.filters.JwtTokenFilter;
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
 * Custom Spring security configuration class for the Feather API
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class FeatherSecurityConfiguration {

    private final ApiKeyFilter apiKeyFilter;
    private final JwtTokenFilter jwtTokenFilter;
    private final ApiKeyOnlyAccessManager apiKeyOnlyAccessManager;
    private final JwtAndApiKeyAccessManager jwtAndApiKeyAccessManager;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * <p>
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(RequestMatchers.NO_AUTH_ACCESSIBLE_ENDPOINTS).permitAll()
                        .requestMatchers(RequestMatchers.API_KEY_ONLY_ACCESSIBLE_ENDPOINTS).access(apiKeyOnlyAccessManager)
                        .anyRequest().access(jwtAndApiKeyAccessManager))
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtTokenFilter, ApiKeyFilter.class);
        return http.build();
    }

}
