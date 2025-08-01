package com.feather.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.feather.api.adapter.posthog.services.PostHogTestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

    @Mock
    private PostHogTestService postHogTestService;

    @InjectMocks
    private TestController classUnderTest;

    @Test
    void testTest1() {
        // Act
        final String test = classUnderTest.test();

        // Assert
        assertThat(test).isEqualTo("Hello, Feather API!");

    }

    @Test
    void testPostHogTest_TracksEvent() {
        // Act
        classUnderTest.postHogTest();

        // Assert
        verify(postHogTestService).trackEventTest();
    }
}