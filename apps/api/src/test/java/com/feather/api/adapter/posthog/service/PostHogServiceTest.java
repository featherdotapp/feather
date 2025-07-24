package com.feather.api.adapter.posthog.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.feather.api.adapter.posthog.PostHogBuilder;
import com.posthog.java.PostHog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostHogServiceTest {

    private static final String TEST_DISTINCT_ID = "user123";
    private static final String TEST_EVENT = "test_event";

    @Mock
    private PostHogBuilder postHogBuilder;

    @Mock
    private PostHog postHog;

    @InjectMocks
    private PostHogService classUnderTest;

    @BeforeEach
    void setUp() {
        when(postHogBuilder.buildPostHog()).thenReturn(postHog);
    }

    @Test
    void testTrackEvent_withProperties_callsPostHogCapture() {
        // Arrange
        final Map<String, Object> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", 42);

        // Act
        classUnderTest.trackEvent(TEST_DISTINCT_ID, TEST_EVENT, properties);

        // Assert
        verify(postHogBuilder).buildPostHog();
        verify(postHog).capture(TEST_DISTINCT_ID, TEST_EVENT, properties);
        verify(postHog).shutdown();
    }

}
