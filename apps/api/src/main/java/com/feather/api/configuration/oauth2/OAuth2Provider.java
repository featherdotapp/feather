package com.feather.api.configuration.oauth2;

import java.util.List;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OAuth2Provider {
    public static final String LINKEDIN_CLIENT_REGISTRATION_ID = "linkedinClientRegistration";

    @Value("${spring.security.oauth2.client.registration.linkedin.client-id}")
    private String linkedinClientId;

    @Value("${spring.security.oauth2.client.registration.linkedin.client-secret}")
    private String linkedinClientSecret;

    @Value("${spring.security.oauth2.client.registration.linkedin.redirect-uri}")
    private String linkedinRedirectUri;

    @Value("${spring.security.oauth2.client.registration.linkedin.scope}")
    private String linkedinScope;

    /**
     * Creates a list of OAuth2 client registrations for Google and LinkedIn.
     * Each registration includes client ID, client secret, redirect URI, and other necessary details.
     *
     * @return a list of {@link ClientRegistration} objects for Google and LinkedIn
     */
    public List<ClientRegistration> getClientRegistrations() {
        ClientRegistration linkedinRegistration = ClientRegistration.withRegistrationId(LINKEDIN_CLIENT_REGISTRATION_ID)
                .clientId(linkedinClientId)
                .clientSecret(linkedinClientSecret)
                .redirectUri(linkedinRedirectUri)
                .authorizationUri("https://www.linkedin.com/oauth/v2/authorization")
                .tokenUri("https://www.linkedin.com/oauth/v2/accessToken")
                .userInfoUri("https://api.linkedin.com/v2/me")
                .userNameAttributeName("id")
                .clientName("LinkedIn")
                .scope(linkedinScope)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();

        return List.of(linkedinRegistration);
    }
}
