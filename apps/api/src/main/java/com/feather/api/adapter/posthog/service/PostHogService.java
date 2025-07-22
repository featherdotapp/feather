package com.feather.api.adapter.posthog.service;

import java.util.Map;

import com.feather.api.adapter.posthog.PostHogBuilder;
import com.posthog.java.PostHog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for interacting with PostHog analytics.
 * This service provides methods to track events in PostHog by using the PostHog client.
 */
@Service
@RequiredArgsConstructor
public class PostHogService {

    private final PostHogBuilder postHogBuilder;

    /**
     * Tracks an event in PostHog.
     *
     * @param distinctId The unique identifier for the user. This can be a user ID, email, or any other unique identifier.
     * @param event The name of the event to track. For example, "authentication_exception".
     * @param properties a {@link Map} containing metadata about the event
     */
    public void trackEvent(final String distinctId, final String event, final Map<String, Object> properties) {
        // TODO: check distinctId
        final PostHog postHog = postHogBuilder.buildPostHog();
        postHog.capture(distinctId, event, properties);
        postHog.shutdown();
    }

}