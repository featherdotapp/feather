package com.feather.api.configuration.constants;

public class RequestMatchers {

    public static final String[] NO_AUTH_ACCESSIBLE_ENDPOINTS = {
            "auth/linkedin/callback"
    };
    public static final String[] API_KEY_ONLY_ACCESSIBLE_ENDPOINTS = {
            "/auth/login",
            "/auth/register",
            "auth/linkedin/loginUrl"
    };

}
