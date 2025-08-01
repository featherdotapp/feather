package com.feather.api.controller;

import com.feather.api.adapter.posthog.services.PostHogTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final PostHogTestService postHogTestService;

    @GetMapping
    public String test() {
        return "Hello, Feather API!";
    }

    @GetMapping("/posthog")
    public void postHogTest() {
        postHogTestService.trackEventTest();
    }

}
