package com.feather.api.security.exception_handling.exception;

/**
 * Exception thrown when there is an error related to resolving or handling endpoint paths
 * in the security configuration or scanning process.
 */

public class PathResolutionException extends RuntimeException {

    /**
     * Creates a Path Resolution Exception
     *
     * @param message message
     */
    public PathResolutionException(final String message) {
        super(message);
    }
}
