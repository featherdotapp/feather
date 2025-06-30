package com.feather.api.configuration.filters.utils;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Represents the result of an authentication attempt.
 * Contains the status of the authentication, user details, a new access token (if refreshed),
 * and an error message (if invalid).
 */
public record AuthResult(Status status, UserDetails userDetails, String newAccessToken, String errorMessage) {

    public enum Status {SUCCESS, REFRESHED, INVALID}

    public static AuthResult success(UserDetails userDetails) {
        return new AuthResult(Status.SUCCESS, userDetails, null, null);
    }

    public static AuthResult refreshed(String newAccessToken) {
        return new AuthResult(Status.REFRESHED, null, newAccessToken, null);
    }

    public static AuthResult invalid(String errorMessage) {
        return new AuthResult(Status.INVALID, null, null, errorMessage);
    }
}
