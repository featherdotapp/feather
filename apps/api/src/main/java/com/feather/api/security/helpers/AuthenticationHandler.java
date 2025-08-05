package com.feather.api.security.helpers;

import java.io.IOException;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import com.feather.api.service.ResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Handles authentication logic using JWT tokens
 */
@Component
@RequiredArgsConstructor
public class AuthenticationHandler {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final ResponseHandler responseHandler;
    private final AuthenticationTokenFactory authenticationTokenFactory;

    /**
     * Authenticates a user based on the provided JWT tokens and sets the security context.
     * If authentication is successful, updated token cookies are added to the response if needed.
     * In case of failure, an error response is handled accordingly.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param apiKey the provided API key
     * @param accessToken the provided access token
     * @param refreshToken the provided refresh token
     * @throws IOException if an I/O error occurs while handling the response
     */
    public void handleAuthentication(final HttpServletRequest request, final HttpServletResponse response, final String apiKey,
            final String accessToken, final String refreshToken) throws IOException {
        try {
            final User user = jwtTokenService.loadUserFromToken(accessToken);
            final FeatherAuthenticationToken providedAuthentication =
                    authenticationTokenFactory.buildAuthenticationTokenFromRequest(apiKey, accessToken, refreshToken, user);
            final Authentication currentAuthentication = authenticationManager.authenticate(providedAuthentication);
            SecurityContextHolder.getContext().setAuthentication(currentAuthentication);
            responseHandler.updateTokenCookiesIfChanged(response, (FeatherCredentials) providedAuthentication.getCredentials(),
                    (FeatherCredentials) currentAuthentication.getCredentials());
        } catch (final JwtAuthenticationException | UsernameNotFoundException e) {
            responseHandler.handleFailureResponse(request, response, e);
        }
    }

}
