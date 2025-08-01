package com.feather.api.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuth2ProviderTest {

    private static final String LINKEDIN_CLIENT_ID = "77aqk5dtm1khzu";
    private static final String LINKEDIN_CLIENT_SECRET = "WPL_AP1.xqrjBN7fJ9Z2Pcad.7WsSgQ==";
    private static final String LINKEDIN_REDIRECT_URI = "http://localhost:8080/auth/linkedin/callback";
    private static final String LINKEDIN_SCOPE = "openid,profile,email";

    @InjectMocks
    private OAuth2Provider classUnderTest;

    /**
     * Spring application properties setup
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(classUnderTest, "linkedinClientId", LINKEDIN_CLIENT_ID);
        ReflectionTestUtils.setField(classUnderTest, "linkedinClientSecret", LINKEDIN_CLIENT_SECRET);
        ReflectionTestUtils.setField(classUnderTest, "linkedinRedirectUri", LINKEDIN_REDIRECT_URI);
        ReflectionTestUtils.setField(classUnderTest, "linkedinScope", LINKEDIN_SCOPE);
    }

    @Test
    void testGetClientRegistrations() {
        // Act
        final List<ClientRegistration> clientRegistrations = classUnderTest.getClientRegistrations();

        // Assert
        assertThat(clientRegistrations).hasSize(1);

        final ClientRegistration linkedinRegistration = clientRegistrations.getFirst();
        assertThat(linkedinRegistration.getClientName()).isEqualTo("LinkedIn");
        assertThat(linkedinRegistration.getRegistrationId()).isEqualTo(OAuth2Provider.LINKEDIN_CLIENT_REGISTRATION_ID);
        assertThat(linkedinRegistration.getClientId()).isEqualTo(LINKEDIN_CLIENT_ID);
        assertThat(linkedinRegistration.getClientSecret()).isEqualTo(LINKEDIN_CLIENT_SECRET);
        assertThat(linkedinRegistration.getRedirectUri()).isEqualTo(LINKEDIN_REDIRECT_URI);
        assertThat(linkedinRegistration.getProviderDetails().getAuthorizationUri()).isEqualTo("https://www.linkedin.com/oauth/v2/authorization");
        assertThat(linkedinRegistration.getProviderDetails().getTokenUri()).isEqualTo("https://www.linkedin.com/oauth/v2/accessToken");
        assertThat(linkedinRegistration.getProviderDetails().getUserInfoEndpoint().getUri()).isEqualTo("https://api.linkedin.com/v2/me");
        assertThat(linkedinRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()).isEqualTo("id");
        assertThat(linkedinRegistration.getScopes()).contains(LINKEDIN_SCOPE);
    }

    @Test
    void testGetLinkedinClientId() {
        // Act
        final String result = classUnderTest.getLinkedinClientId();

        // Assert
        assertThat(result).isEqualTo(LINKEDIN_CLIENT_ID);
    }

    @Test
    void testGetLinkedinRedirectUri() {
        // Act
        final String result = classUnderTest.getLinkedinRedirectUri();

        // Assert
        assertThat(result).isEqualTo(LINKEDIN_REDIRECT_URI);
    }

    @Test
    void testGetLinkedinScope() {
        // Act
        final String result = classUnderTest.getLinkedinScope();

        // Assert
        assertThat(result).isEqualTo(LINKEDIN_SCOPE);
    }

    @Test
    void testGetLinkedinSecret() {
        // Act
        final String result = classUnderTest.getLinkedinClientSecret();

        // Assert
        assertThat(result).isEqualTo(LINKEDIN_CLIENT_SECRET);
    }
}