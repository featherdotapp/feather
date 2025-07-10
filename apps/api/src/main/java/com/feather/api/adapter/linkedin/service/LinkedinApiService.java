package com.feather.api.adapter.linkedin.service;

import com.feather.api.adapter.linkedin.dto.LinkedInTokenResponse;
import com.feather.api.adapter.linkedin.dto.LinkedinUserInfoResponseDTO;
import com.feather.api.security.oauth2.OAuth2Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Service class that handles LinkedIn OAuth2 authentication and API interactions.
 * This service provides methods to exchange authorization codes for access tokens
 * and retrieve user information from LinkedIn's API.
 */
@Service
@RequiredArgsConstructor
public class LinkedinApiService {

    public static final String LINKEDIN_ACCESS_TOKEN_EXCHANGE_URL = "https://www.linkedin.com/oauth/v2/accessToken";
    private static final String LINKEDIN_MEMBER_DETAILS_URL = "https://api.linkedin.com/v2/userinfo";

    private final OAuth2Provider oAuth2Provider;

    /**
     * Exchanges an OAuth2 authorization code for an access token using LinkedIn's OAuth2 endpoint.
     *
     * @param authorizationCode The authorization code received from LinkedIn's OAuth2 authorization endpoint
     * @return LinkedInTokenResponse containing the access token and related information
     */
    public LinkedInTokenResponse exchangeAuthorizationCodeForAccessToken(String authorizationCode) {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final HttpEntity<MultiValueMap<String, String>> request = getMultiValueMapHttpEntity(authorizationCode, headers);
        final ResponseEntity<LinkedInTokenResponse> response = restTemplate.exchange(
                LINKEDIN_ACCESS_TOKEN_EXCHANGE_URL,
                HttpMethod.POST,
                request,
                LinkedInTokenResponse.class
        );
        return response.getBody();
    }

    private HttpEntity<MultiValueMap<String, String>> getMultiValueMapHttpEntity(final String authorizationCode, final HttpHeaders headers) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", authorizationCode);
        requestBody.add("client_id", oAuth2Provider.getLinkedinClientId());
        requestBody.add("client_secret", oAuth2Provider.getLinkedinClientSecret());
        requestBody.add("redirect_uri", oAuth2Provider.getLinkedinRedirectUri());
        return new HttpEntity<>(requestBody, headers);
    }

    /**
     * Retrieves user information from LinkedIn's userinfo endpoint using the provided access token.
     *
     * @param bearerAccessToken The OAuth2 access token to authenticate the request
     * @return LinkedinUserInfoResponseDTO containing the user's profile information
     */
    public LinkedinUserInfoResponseDTO getMemberDetails(String bearerAccessToken) {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerAccessToken);
        final HttpEntity<?> request = new HttpEntity<>(headers);
        final ResponseEntity<LinkedinUserInfoResponseDTO> response = restTemplate.exchange(
                LINKEDIN_MEMBER_DETAILS_URL,
                HttpMethod.GET,
                request,
                LinkedinUserInfoResponseDTO.class
        );
        return response.getBody();
    }
}
