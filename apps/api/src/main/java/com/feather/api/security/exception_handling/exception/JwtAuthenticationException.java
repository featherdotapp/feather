package com.feather.api.security.exception_handling.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when JWT authentication fails.
 * Extends {@link AuthenticationException} from Spring Security.
 */
public class JwtAuthenticationException extends AuthenticationException {

    /**
     * Constructs a new JwtAuthenticationException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public JwtAuthenticationException(final String message) {
        super(message);
    }
}
