package com.feather.api.security.configurations.request_matchers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Constant class which stores RequestsMatchers used in the SecurityFilterChain
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMatcherProvider {


    private static final String[] NO_SECURED_ENDPOINTS = {
            "/auth/linkedin/callback",
            "/auth/test"
    };

    private static final String[] API_KEY_SECURED_ENDPOINTS = {
            "/auth/login",
            "/auth/register",
            "/auth/linkedin/loginUrl"
    };

    public static final RequestMatcher NO_AUTH_REQUEST_MATCHERS = RequestMatcherBuilder
            .create()
            .withPaths(NO_SECURED_ENDPOINTS)
            .asOrMatcher();

    public static final RequestMatcher API_KEY_AUTH_REQUEST_MATCHERS = RequestMatcherBuilder
            .create()
            .withPaths(API_KEY_SECURED_ENDPOINTS)
            .asOrMatcher();

    public static final RequestMatcher API_AND_JWT_SECURED_ENDPOINTS = RequestMatcherBuilder
            .create()
            .withPaths(NO_SECURED_ENDPOINTS, API_KEY_SECURED_ENDPOINTS)
            .asNegatedMatcher();
}
