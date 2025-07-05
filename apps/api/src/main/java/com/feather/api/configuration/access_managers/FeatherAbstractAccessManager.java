package com.feather.api.configuration.access_managers;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

public abstract class FeatherAbstractAccessManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationManager.super.verify(authentication, object);
    }

    /**
     * Deprecated method for authorization checks.
     */
    @Deprecated
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        return null;
    }

    @Override
    public AuthorizationResult authorize(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        return AuthorizationManager.super.authorize(authentication, object);
    }
}
