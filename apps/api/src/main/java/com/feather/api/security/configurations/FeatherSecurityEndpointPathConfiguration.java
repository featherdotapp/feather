package com.feather.api.security.configurations;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.feather.api.security.annotations.ApiKeyAuthenticated;
import com.feather.api.security.annotations.FullyAuthenticated;
import com.feather.api.security.annotations.Unauthenticated;
import com.feather.api.security.configurations.model.EndpointPaths;
import com.feather.api.security.exception_handling.exception.PathResolutionException;
import com.feather.api.security.helpers.PathResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RestController;

/**
 * Scans all Spring RestController beans for endpoint paths and categorizes them
 * based on authentication requirements (no authentication, full authentication, API key).
 * <p>
 * Populates lists of endpoint paths for each authentication type after the application startup.
 */
@Configuration
@RequiredArgsConstructor
public class FeatherSecurityEndpointPathConfiguration {

    private final ApplicationContext applicationContext;
    private final PathResolver pathResolver;

    /**
     * Singleton bean for EndpointPaths to ensure only one instance exists in the application context.
     * Scans all RestController beans and their methods for authentication annotations
     * and populates the corresponding path lists for each authentication type.
     * This method should be called before the application is fully initialized.
     */
    @Bean
    @Scope("singleton")
    protected EndpointPaths applicationPaths() {
        final Set<String> unauthenticatedPaths = new HashSet<>();
        final Set<String> fullAuthenticationPaths = new HashSet<>();
        final Set<String> apiKeyPaths = new HashSet<>();
        final Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RestController.class);

        for (final Object bean : beans.values()) {
            final Class<?> clazz = bean.getClass();
            for (final Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(FullyAuthenticated.class)) {
                    final String resolvedPath = pathResolver.resolvePath(method, clazz);
                    addPathToSet(fullAuthenticationPaths, apiKeyPaths, unauthenticatedPaths, resolvedPath);
                } else if (method.isAnnotationPresent(ApiKeyAuthenticated.class)) {
                    final String resolvedPath = pathResolver.resolvePath(method, clazz);
                    addPathToSet(apiKeyPaths, fullAuthenticationPaths, unauthenticatedPaths, resolvedPath);
                } else if (method.isAnnotationPresent(Unauthenticated.class)) {
                    final String resolvedPath = pathResolver.resolvePath(method, clazz);
                    addPathToSet(unauthenticatedPaths, fullAuthenticationPaths, apiKeyPaths, resolvedPath);
                }
            }
        }
        return new EndpointPaths(
                fullAuthenticationPaths.toArray(String[]::new),
                apiKeyPaths.toArray(String[]::new),
                unauthenticatedPaths.toArray(String[]::new)
        );
    }

    private void addPathToSet(final Set<String> targetSet, final Set<String> set1, final Set<String> set2, final String resolvedPath) {
        if (set1.contains(resolvedPath) || set2.contains(resolvedPath)) {
            throw new PathResolutionException(PathResolutionException.ErrorType.DUPLICATE_PATH, resolvedPath);
        }
        targetSet.add(resolvedPath);
    }

}
