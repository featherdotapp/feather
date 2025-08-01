package com.feather.api.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated endpoint requires API key authentication.
 * <p>
 * Methods annotated with this will only be accessible if a valid API key is provided.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiKeyAuthenticated {

}
