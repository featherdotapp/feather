package com.feather.api.security.exception_handling.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when JWT authentication fails.
 * Extends {@link AuthenticationException} from Spring Security.
 */
public class JwtAuthenticationException extends AuthenticationException {

    public static final String EXPIRED_REFRESH_TOKEN = "Expired Refresh Token, log in again to get a new Refresh Token.";
    public static final String INVALID_ACCESS_TOKEN = "Access Token is invalid";
    public static final String INVALID_REFRESH_TOKEN = "Refresh Token is invalid";

    /**
     * Constructs a new JwtAuthenticationException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public JwtAuthenticationException(final String message) {
        super(message);
    }
}
