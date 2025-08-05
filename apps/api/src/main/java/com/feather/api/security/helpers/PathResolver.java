package com.feather.api.security.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.feather.api.security.exception_handling.exception.PathResolutionException;
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

    private static final List<Class<? extends Annotation>> PATH_ANNOTATIONS = List.of(
            GetMapping.class,
            PostMapping.class,
            PutMapping.class,
            DeleteMapping.class
    );
    public static final String REQUEST_MAPPING = "value";

    /**
     * Resolves the full path for a given method, including any class-level request mapping.
     *
     * @param method the method for which to resolve the path
     * @param clazz the class containing the method
     * @return the resolved path as a String
     */
    public String resolvePath(final Method method, final Class<?> clazz) {
        final StringBuilder classRequestMapping = resolveClassRequestMapping(clazz);
        for (final Class<? extends Annotation> mappingType : PATH_ANNOTATIONS) {
            if (method.isAnnotationPresent(mappingType)) {
                final Annotation methodAnnotation = method.getAnnotation(mappingType);
                return resolveMethodPath(method, mappingType, methodAnnotation, classRequestMapping);
            }
        }
        throw new PathResolutionException(PathResolutionException.ErrorType.NO_VALID_MAPPING,
                method.getName(), PATH_ANNOTATIONS);
    }

    private String resolveMethodPath(final Method method, final Class<? extends Annotation> mappingType,
            final Annotation methodAnnotation, final StringBuilder classRequestMapping) {
        final String methodName = method.getName();
        final String annotationName = mappingType.getSimpleName();
        try {
            final Method valueMethod = mappingType.getMethod(REQUEST_MAPPING);
            final String[] value = (String[]) valueMethod.invoke(methodAnnotation);
            if (value.length > 1) {
                throw new PathResolutionException(PathResolutionException.ErrorType.MULTI_PATH_NOT_SUPPORTED, methodName);
            } else if (value.length == 0) {
                throw new PathResolutionException(PathResolutionException.ErrorType.EMPTY_PATH_VALUE, annotationName, methodName);
            } else {
                final String[] subPaths = value[0].split("/");
                final String endpointPath = resolveSubPaths(subPaths);
                return classRequestMapping.append(endpointPath).toString();
            }
        } catch (final NoSuchMethodException e) {
            throw new PathResolutionException(PathResolutionException.ErrorType.NO_VALUE_METHOD, methodName, annotationName);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new PathResolutionException(PathResolutionException.ErrorType.VALUE_ACCESS_ERROR, annotationName, methodName);
        }
    }

    /**
     * Builds an Ant-style endpoint path from the given sub-paths array.
     * <p>
     * If a path variable (e.g., {id}) is encountered, appends '/**' and stops further processing.
     *
     * @param subPaths the segments of the path
     * @return the resolved endpoint path as a String
     */
    private String resolveSubPaths(final String[] subPaths) {
        final StringBuilder endpointPath = new StringBuilder();
        boolean foundPathVariable = false;
        int pathIndex = getPathIndex(subPaths);
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

    /**
     * Determines the starting index for processing sub-paths.
     * <p>
     * If the first segment is empty (e.g., leading slash), starts from index 1; otherwise, starts from index 0.
     */
    private int getPathIndex(String[] subPaths) {
        return subPaths[0].isEmpty() ? 1 : 0;
    }

    private boolean isPathVariableSegment(final String subPath) {
        return subPath.startsWith("{") && subPath.endsWith("}");
    }

    private StringBuilder resolveClassRequestMapping(final Class<?> clazz) {
        if (clazz.isAnnotationPresent(RequestMapping.class)) {
            final RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
            if (classMapping.value().length > 0) {
                return new StringBuilder().append(classMapping.value()[0]);
            }
        }
        return new StringBuilder();
    }
}
