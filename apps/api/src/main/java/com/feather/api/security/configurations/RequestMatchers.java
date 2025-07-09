package com.feather.api.security.configurations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Constant class which stores RequestsMatchers used in the SecurityFilterChain
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMatchers {

    protected static final String[] NO_SECURED_ENDPOINTS = {
            "auth/linkedin/callback",
    };

    protected static final String[] API_KEY_SECURED_ENDPOINTS = {
            "/auth/login",
            "/auth/register",
            "auth/linkedin/loginUrl"
    };

    protected static final RequestMatcher API_AND_JWT_SECURED_ENDPOINTS= createNegated();

    private static RequestMatcher createNegated() {
        List<String> excludedPaths = new ArrayList<>();
        excludedPaths.addAll(Arrays.asList(RequestMatchers.NO_SECURED_ENDPOINTS));
        excludedPaths.addAll(Arrays.asList(RequestMatchers.API_KEY_SECURED_ENDPOINTS));
        RequestMatcher excludedMatcher = new OrRequestMatcher(
                excludedPaths.stream()
                        .map(path -> new RegexRequestMatcher(path, null))
                        .toArray(RequestMatcher[]::new));
        return new NegatedRequestMatcher(excludedMatcher);
    }
}
