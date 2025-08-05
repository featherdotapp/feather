package com.feather.api.security.exception_handling.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when API key authentication fails.
 * Extends {@link AuthenticationException} from Spring Security.
 */
public class ApiKeyAuthenticationException extends AuthenticationException {

    /**
     * Constructs a new ApiKeyAuthenticationException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ApiKeyAuthenticationException(final String message) {
        super(message);
    }
}
