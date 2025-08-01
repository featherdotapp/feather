package com.feather.api.adapter.linkedin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feather.api.adapter.linkedin.dto.LinkedInTokenResponse;
import com.feather.api.adapter.linkedin.dto.LinkedinUserInfoResponseDTO;
import com.feather.api.security.oauth2.OAuth2Provider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class LinkedinApiServiceTest {

    public static final String LINKEDIN_CLIENT_ID = "77aqk5dtm1khzu";
    public static final String LINKEDIN_CLIENT_SECRET = "WPL_AP1.xqrjBN7fJ9Z2Pcad.7WsSgQ==";
    public static final String LINKEDIN_REDIRECT_URI = "=http://localhost:8080/auth/linkedin/callback";
    private static final String LINKEDIN_ACCESS_TOKEN_EXCHANGE_URL = "https://www.linkedin.com/oauth/v2/accessToken";
    private static final String LINKEDIN_MEMBER_DETAILS_URL = "https://api.linkedin.com/v2/userinfo";

    @Mock
    private OAuth2Provider oAuth2Provider;

    @InjectMocks
    private LinkedinApiService classUnderTest;

    @Test
    void testExchangeAuthorizationCodeForAccessToken() {
        // Arrange
        final String authorizationCode = "authorizationCode";
        final LinkedInTokenResponse expectedResponse = new LinkedInTokenResponse("at", 1000, "someScope", "user");

        when(oAuth2Provider.getLinkedinClientId()).thenReturn(LINKEDIN_CLIENT_ID);
        when(oAuth2Provider.getLinkedinClientSecret()).thenReturn(LINKEDIN_CLIENT_SECRET);
        when(oAuth2Provider.getLinkedinRedirectUri()).thenReturn(LINKEDIN_REDIRECT_URI);

        // Mock the RestTemplate that is created inside the method
        try (final MockedConstruction<RestTemplate> mockedRestTemplate = Mockito.mockConstruction(
                RestTemplate.class,
                (mock, context) ->
                        when(mock.exchange(
                                eq(LINKEDIN_ACCESS_TOKEN_EXCHANGE_URL),
                                eq(HttpMethod.POST),
                                any(HttpEntity.class),
                                eq(LinkedInTokenResponse.class)
                        )).thenAnswer(invocation -> {
                            // Capture and verify the request body
                            HttpEntity<MultiValueMap<String, String>> request = invocation.getArgument(2);
                            HttpHeaders headers = request.getHeaders();
                            MultiValueMap<String, String> body = request.getBody();

                            assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);
                            assertRequestBody(body);

                            return ResponseEntity.ok(expectedResponse);
                        })
        )) {

            // Act
            final LinkedInTokenResponse actualResponse = classUnderTest.exchangeAuthorizationCodeForAccessToken(authorizationCode);

            // Assert
            assertThat(actualResponse).isEqualTo(expectedResponse);
            final RestTemplate mockRestTemplate = mockedRestTemplate.constructed().getFirst();
            verify(mockRestTemplate).exchange(
                    eq(LINKEDIN_ACCESS_TOKEN_EXCHANGE_URL),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(LinkedInTokenResponse.class)
            );
        }
    }

    private void assertRequestBody(final MultiValueMap<String, String> body) {
        assertThat(body).isNotNull();
        assertThat(body.getFirst("grant_type")).isEqualTo("authorization_code");
        assertThat(body.getFirst("code")).isEqualTo("authorizationCode");
        assertThat(body.getFirst("client_id")).isEqualTo(LINKEDIN_CLIENT_ID);
        assertThat(body.getFirst("client_secret")).isEqualTo(LINKEDIN_CLIENT_SECRET);
        assertThat(body.getFirst("redirect_uri")).isEqualTo(LINKEDIN_REDIRECT_URI);
    }

    @Test
    void testGetMemberDetails() {
        // Arrange
        final String accessToken = "accessToken";
        final LinkedinUserInfoResponseDTO expectedResponse = mock(LinkedinUserInfoResponseDTO.class);
        try (final MockedConstruction<RestTemplate> mockedRestTemplate = Mockito.mockConstruction(
                RestTemplate.class,
                (mock, context) ->
                        when(mock.exchange(
                                eq(LINKEDIN_MEMBER_DETAILS_URL),
                                eq(HttpMethod.GET),
                                any(HttpEntity.class),
                                eq(LinkedinUserInfoResponseDTO.class)
                        )).thenAnswer(invocation -> {
                            HttpEntity<?> request = invocation.getArgument(2);
                            HttpHeaders headers = request.getHeaders();

                            assertThat(headers.getFirst("Authorization")).isEqualTo("Bearer " + accessToken);

                            return ResponseEntity.ok(expectedResponse);
                        })
        )) {

            // Act
            final LinkedinUserInfoResponseDTO actualResponse = classUnderTest.getMemberDetails(accessToken);

            // Assert
            assertThat(actualResponse).isEqualTo(expectedResponse);
            final RestTemplate mockRestTemplate = mockedRestTemplate.constructed().getFirst();
            verify(mockRestTemplate).exchange(
                    eq(LINKEDIN_MEMBER_DETAILS_URL),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(LinkedinUserInfoResponseDTO.class)
            );
        }
    }
}