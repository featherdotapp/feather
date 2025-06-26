package com.feather.api.adapter.posthog;

import org.springframework.beans.factory.annotation.Value;
import com.posthog.java.PostHog;
import org.springframework.stereotype.Component;

/**
 * Configuration class for PostHog integration.
 * Initializes the PostHog client with the API key and host from application properties and shuts
 * it down gracefully on application shutdown.
 */
@Component
public class PostHogBuilder {

    @Value("${spring.posthog.api.key}")
    private String posthogApiKey;

    @Value("${spring.posthog.host}")
    private String postHogHost;

    public PostHog buildPostHog() {
        return new PostHog.Builder(posthogApiKey).host(postHogHost).build();
    }

}
