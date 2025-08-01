package com.feather.api.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants related to the application Authentication
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthenticationConstants {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "Refresh-Token";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "A-Token";
}
