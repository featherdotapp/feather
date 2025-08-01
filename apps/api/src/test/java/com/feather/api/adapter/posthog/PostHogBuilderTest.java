package com.feather.api.adapter.posthog;

import static org.assertj.core.api.Assertions.assertThat;

import com.posthog.java.PostHog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostHogBuilderTest {

    private static final String POST_HOG_API_KEY = "test-api-key";
    private static final String POST_HOG_HOST = "https://eu.posthog.com";

    @InjectMocks
    private PostHogBuilder classUnderTest;

    @Test
    void testBuildPostHog_withDifferentValues_returnsCorrectlyConfiguredClient() {
        // Arrange

        ReflectionTestUtils.setField(classUnderTest, "posthogApiKey", POST_HOG_API_KEY);
        ReflectionTestUtils.setField(classUnderTest, "postHogHost", POST_HOG_HOST);

        // Act
        final PostHog result = classUnderTest.buildPostHog();

        // Assert
        assertThat(result).isNotNull();
    }
}
