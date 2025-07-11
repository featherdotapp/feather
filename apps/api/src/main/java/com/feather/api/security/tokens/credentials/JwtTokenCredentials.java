package com.feather.api.security.tokens.credentials;

public record JwtTokenCredentials(String accessToken, String refreshToken) {

}
