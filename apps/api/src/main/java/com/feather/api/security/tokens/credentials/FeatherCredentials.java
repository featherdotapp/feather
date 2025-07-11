package com.feather.api.security.tokens.credentials;

/**
 * Represents the credentials used for authentication in the Feather application.
 * This record encapsulates the API key, access token, and refresh token.
 */
public record FeatherCredentials(String apiKey, String accessToken, String refreshToken) {

}
