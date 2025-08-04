package com.feather.api.security.configurations;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Custom Cross-Origin Resource Sharing configuration, initializes configuration depending on profile
 */
@Configuration
public class FeatherCorsConfiguration {

    @Value("${app.cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    /**
     * Creates a CorsConfigurationSource for the dev profile
     *
     * @return a CorsConfigurationSource with production-specific cors configuration
     */
    @Bean
    @Primary
    @Profile("dev")
    public CorsConfigurationSource devCorsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:63342", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        return provideConfigurationSource(config);
    }

    /**
     * Creates a CorsConfigurationSource for the production profile
     *
     * @return a CorsConfigurationSource with production specific cors configuration
     */
    @Bean
    @Primary
    @Profile("prod")
    public CorsConfigurationSource prodCorsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        return provideConfigurationSource(config);
    }

    private UrlBasedCorsConfigurationSource provideConfigurationSource(final CorsConfiguration config) {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
