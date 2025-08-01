package com.feather.api.adapter.linkedin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO containing the LinkedIn User information
 * This class maps the JSON response received when calling the userinfo endpoint from the LinkedIn API.
 */
public record LinkedinUserInfoResponseDTO(@JsonProperty("sub") String subjectIdentifier,
                                          @JsonProperty("name") String fullName,
                                          @JsonProperty("given_name") String firstName,
                                          @JsonProperty("family_name") String lastName,
                                          @JsonProperty("picture") String picture,
                                          @JsonProperty("locale") Object locale,
                                          @JsonProperty("email") String email,
                                          @JsonProperty("email_verified") boolean isEmailVerified
) {

}

