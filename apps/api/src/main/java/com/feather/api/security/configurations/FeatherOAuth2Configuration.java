package com.feather.api.security.configurations;

import java.util.List;

import com.feather.api.security.oauth2.OAuth2Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * Configuration for OAuth2 clients
 */
@Configuration
@RequiredArgsConstructor
public class FeatherOAuth2Configuration {

    private final OAuth2Provider oAuth2Provider;

    /**
     * Creates a {@link ClientRegistrationRepository} containing the OAuth2 client registrations provided by the
     * {@link OAuth2Provider#getClientRegistrations()}, using credentials and redirect URI from application properties.
     *
     * @return an in-memory {@link ClientRegistrationRepository} configured for Google OAuth2 login
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        final List<ClientRegistration> registrations = oAuth2Provider.getClientRegistrations();
        return new InMemoryClientRegistrationRepository(registrations);
    }

    /**
     * Creates an {@link OAuth2AuthorizedClientService} to manage authorized clients in memory.
     *
     * @param clientRegistrationRepository the repository containing client registrations
     * @return an in-memory {@link OAuth2AuthorizedClientService}
     */
    @Bean
    public OAuth2AuthorizedClientService customAuthorizedClientService(final ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }
}
