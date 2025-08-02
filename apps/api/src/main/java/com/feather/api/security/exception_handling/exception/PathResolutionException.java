package com.feather.api.security.exception_handling.exception;

/**
 * Exception thrown when there is an error related to resolving or handling endpoint paths
 * in the security configuration or scanning process.
 */

public class PathResolutionException extends RuntimeException {

    public enum ErrorType {
        NO_VALID_MAPPING("The method '%s' does not have a valid mapping annotation. Supported annotations: %s"),
        MULTI_PATH_NOT_SUPPORTED("Multi path mapping is not supported. Check the '%s' method and ensure the mapping annotation has only 1 valid path value."),
        EMPTY_PATH_VALUE(
                "The mapping annotation '%s' in method '%s' does not have a valid path value. Ensure it is properly configured with a non-empty path."),
        NO_VALUE_METHOD("Unable to find the 'value' method in the mapping annotation for method '%s'. Ensure the annotation %s has a value() method defined."),
        VALUE_ACCESS_ERROR(
                "Unable to access the value from %s annotation in method '%s'. Check if the annotation is properly configured with a valid path value."),
        DUPLICATE_PATH("Endpoint path '%s' is assigned to multiple authentication levels.");

        private final String messageTemplate;

        ErrorType(String messageTemplate) {
            this.messageTemplate = messageTemplate;
        }

        public String formatMessage(Object... args) {
            return String.format(messageTemplate, args);
        }
    }

    /**
     * Creates a Path Resolution Exception
     *
     * @param errorType error type
     * @param args optional arguments for the error message
     */
    public PathResolutionException(ErrorType errorType, Object... args) {
        super(errorType.formatMessage(args));
    }
}
