package com.feather.api.security.tokens.credentials;

/**
 * Represents the JWT token credentials used for authentication in the Feather application.
 * This record encapsulates the access token and refresh token.
 */
public record JwtTokenCredentials(String accessToken, String refreshToken) {

}
