package com.feather.api.adapter.linkedin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Record class representing the response from LinkedIn's OAuth2 token endpoint.
 * This class maps the JSON response received when exchanging an authorization code
 * for an access token.
 */
public record LinkedInTokenResponse(@JsonProperty("access_token") String accessToken,
                                    @JsonProperty("expires_in") Integer expiresIn,
                                    @JsonProperty("scope") String scope,
                                    @JsonProperty("token_type") String tokenType) {

}
