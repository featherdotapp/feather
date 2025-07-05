package com.feather.api.configuration.access_managers;

import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ApiKeyOnlyAccessManager extends FeatherAbstractAccessManager {

    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        super.verify(authentication, object);
    }

    @Override
    public AuthorizationResult authorize(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        return super.authorize(authentication, object);
    }
}
