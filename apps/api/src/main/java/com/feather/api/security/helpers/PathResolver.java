package com.feather.api.security.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Utility class for resolving endpoint paths and class-level request mappings in Spring controllers.
 * <p>
 * Provides methods to build Ant-style endpoint patterns from path segments and to extract base paths
 * from @RequestMapping annotations.
 */
@Component
public class PathResolver {

    // TODO: TEST
    private static final List<Class<? extends Annotation>> PATH_ANNOTATIONS = List.of(
            GetMapping.class,
            PostMapping.class,
            PutMapping.class,
            DeleteMapping.class
    );

    /**
     * Resolves the full path for a given method, including any class-level request mapping.
     *
     * @param method the method for which to resolve the path
     * @param clazz the class containing the method
     * @return the resolved path as a String
     */
    public String resolvePath(final Method method, final Class<?> clazz) {
        final String basePath = resolveClassRequestMapping(clazz);

        for (final Class<? extends Annotation> mappingType : PATH_ANNOTATIONS) {
            if (method.isAnnotationPresent(mappingType)) {
                final Annotation methodAnnotation = method.getAnnotation(mappingType);
                try {
                    final Method valueMethod = mappingType.getMethod("value");
                    final String[] values = (String[]) valueMethod.invoke(methodAnnotation);
                    if (values.length > 0) {
                        final String[] subPaths = values[0].split("/");
                        final String endpointPath = resolveEndpointPath(subPaths);
                        return basePath + endpointPath;
                    }
                } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    System.err.println("[WARN] Error when resolving path for method: " + method.getName());
                }
            }
        }
        return basePath;
    }

    /**
     * Builds an Ant-style endpoint path from the given sub-paths array.
     * <p>
     * If a path variable (e.g., {id}) is encountered, appends '/**' and stops further processing.
     *
     * @param subPaths the segments of the path
     * @return the resolved endpoint path as a String
     */
    private String resolveEndpointPath(final String[] subPaths) {
        final StringBuilder endpointPath = new StringBuilder();
        boolean foundPathVariable = false;
        int pathIndex = 1;
        while (!foundPathVariable && pathIndex < subPaths.length) {
            final String subPath = subPaths[pathIndex];
            foundPathVariable = isPathVariableSegment(subPath);
            if (foundPathVariable) {
                endpointPath.append("/**");
            } else {
                endpointPath.append("/").append(subPath);
            }
            pathIndex++;
        }
        return endpointPath.toString();
    }

    private boolean isPathVariableSegment(final String subPath) {
        return subPath.startsWith("{") && subPath.endsWith("}");
    }

    /**
     * Resolves the base path from a class annotated with @RequestMapping.
     *
     * @param clazz the controller class
     * @return the base path if present, otherwise an empty string
     */
    public String resolveClassRequestMapping(final Class<?> clazz) {
        if (clazz.isAnnotationPresent(RequestMapping.class)) {
            final RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
            if (classMapping.value().length > 0) {
                return classMapping.value()[0];
            }
        }
        return "";
    }
}
