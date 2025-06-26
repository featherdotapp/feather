package com.feather.api.adapter.posthog.services;

import com.feather.api.adapter.posthog.PostHogBuilder;
import com.posthog.java.PostHog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostHogTestService {

    private final PostHogBuilder postHogBuilder;

    public void trackEventTest() {
        PostHog postHog = postHogBuilder.buildPostHog();
        postHog.capture("distinct_id_of_the_user", "user_signed_up");
        postHog.shutdown();
    }
}
