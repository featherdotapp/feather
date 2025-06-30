package com.feather.api.controller;

import com.feather.api.configuration.oauth2.OAuth2Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationControllerController {

    private final OAuth2Provider oAuth2Provider;

    @PostMapping("/linkedin/loginUrl")
    public String linkedinLoginUrl() {
        return "https://www.linkedin.com/oauth/v2/authorization" +
                "?response_type=code" +
                "&client_id=" + oAuth2Provider.getLinkedinClientId() +
                "&redirect_uri=" + oAuth2Provider.getLinkedinRedirectUri() +
                "&scope=" + oAuth2Provider.getLinkedinScope();
    }

    @PostMapping("/linkedin/callback")
    public String linkedinCallback() {
        // TODO: handle callback
        return "";
    }
}
