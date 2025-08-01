package com.feather.api.security.configurations.model;

import java.util.Arrays;
import java.util.Objects;

import lombok.NonNull;

/**
 * Holds arrays of endpoint paths categorized by their authentication requirements.
 * <p>
 * This class is used to provide the security configuration with the lists of endpoint paths
 * that require full authentication, API key authentication, or no authentication.
 */
public record EndpointPaths(String[] fullyAuthenticatedPaths, String[] apiKeyAuthenticatedPaths, String[] unauthenticatedPaths) {

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EndpointPaths that = (EndpointPaths) o;
        return Objects.deepEquals(unauthenticatedPaths, that.unauthenticatedPaths) &&
                Objects.deepEquals(fullyAuthenticatedPaths, that.fullyAuthenticatedPaths) &&
                Objects.deepEquals(apiKeyAuthenticatedPaths, that.apiKeyAuthenticatedPaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Arrays.hashCode(fullyAuthenticatedPaths),
                Arrays.hashCode(apiKeyAuthenticatedPaths),
                Arrays.hashCode(unauthenticatedPaths)
        );
    }

    @Override
    @NonNull
    public String toString() {
        return "EndpointPaths{" +
                "fullyAuthenticatedPaths=" + Arrays.toString(fullyAuthenticatedPaths) +
                ", apiKeyAuthenticatedPaths=" + Arrays.toString(apiKeyAuthenticatedPaths) +
                ", unauthenticatedPaths=" + Arrays.toString(unauthenticatedPaths) +
                '}';
    }
}