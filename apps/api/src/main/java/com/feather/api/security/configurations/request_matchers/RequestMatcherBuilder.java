package com.feather.api.security.configurations.request_matchers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * A builder class for creating composite {@link RequestMatcher} instances
 * to be used in Spring Security configurations.
 * <p>
 * This builder supports building matchers that combine multiple path patterns,
 * optionally scoped to a specific HTTP method, and allows creating
 * either an {@link OrRequestMatcher} (logical OR of matchers) or
 * a {@link NegatedRequestMatcher} (negation of an OR matcher).
 * <p>
 */
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMatcherBuilder {

    // TODO: add  ParameterRequestMatcher to provide common matchers

    private final List<RequestMatcher> matchers = new ArrayList<>();
    private String method = null; // HTTP method (null = all methods)


    public static RequestMatcherBuilder create() {
        return new RequestMatcherBuilder();
    }

    /**
     * Specifies the HTTP method to scope the matchers.
     * If not set, matchers will apply to all HTTP methods.
     *
     * @param method the HTTP method (e.g. "GET", "POST"), or null for any
     * @return the current builder instance for method chaining
     */
    public RequestMatcherBuilder withHttpMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * Adds one or more path patterns to be matched.
     * Uses {@link RegexRequestMatcher} internally with optional HTTP method.
     *
     * @param paths one or more path regex patterns
     * @return the current builder instance for method chaining
     */
    public RequestMatcherBuilder withPaths(String... paths) {
        matchers.addAll(
                Arrays.stream(paths)
                        .map(path -> new RegexRequestMatcher(path, method))
                        .toList()
        );
        return this;
    }


    /**
     * Adds one or more lists of path patterns to be matched.
     * Flattens all lists and uses {@link RegexRequestMatcher} internally with optional HTTP method.
     *
     * @param paths one or more lists of path regex patterns
     * @return the current builder instance for method chaining
     */
    public RequestMatcherBuilder withPaths(String[]... paths) {
        Stream<String> flattenedPaths = Arrays.stream(paths).flatMap(Arrays::stream);
        flattenedPaths.forEach(path -> matchers.add(new RegexRequestMatcher(path, method)));
        return this;
    }

    /**
     * Builds an {@link OrRequestMatcher} which matches if any of the added
     * path patterns match.
     *
     * @return a composite {@link RequestMatcher} combining all added matchers with OR logic
     */
    public RequestMatcher asOrMatcher() {
        return new OrRequestMatcher(matchers);
    }

    /**
     * Builds a {@link NegatedRequestMatcher} which matches if none of the added
     * path patterns match (logical negation of OR matcher).
     *
     * @return a negated composite {@link RequestMatcher}
     */
    public RequestMatcher asNegatedMatcher() {
        return new NegatedRequestMatcher(asOrMatcher());
    }
}
