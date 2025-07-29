package com.feather.api.security.filters;

import static com.feather.api.shared.AuthenticationConstants.AUTHORIZATION_HEADER;
import static com.feather.api.shared.AuthenticationConstants.BEARER_PREFIX;
import static com.feather.api.shared.AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME;

import java.io.IOException;
import java.util.Optional;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.security.exception_handling.FeatherAuthenticationEntryPoint;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.security.tokens.ApiKeyAuthenticationToken;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import com.feather.api.service.CookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter for JWT-based authentication.
 * <p>
 * Extracts the JWT from the Authorization header and authenticates it using the AuthenticationManager.
 * If the JWT is valid, sets the authentication in the SecurityContext.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final CookieService cookieService;
    private final JwtTokenService jwtTokenService;
    private final FeatherAuthenticationEntryPoint authenticationEntryPoint;

    private static boolean hasBearerPrefix(final String token) {
        return token.startsWith(BEARER_PREFIX);
    }

    /**
     * Performs JWT authentication for each request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain)
            throws ServletException, IOException {
        final String accessToken = request.getHeader(AUTHORIZATION_HEADER);
        final Optional<Cookie> refreshTokenCookie = cookieService.findCookie(request.getCookies(), REFRESH_TOKEN_COOKIE_NAME);
        final String apiKey = getApiKey();
        try {
            if (refreshTokenCookie.isPresent() && accessToken != null) {
                final String refreshToken = refreshTokenCookie.get().getValue();
                if (!refreshToken.isEmpty() && hasBearerPrefix(accessToken)) {
                    handleAuthentication(response, apiKey, accessToken, refreshToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (final JwtAuthenticationException | UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, e);
        }
    }

    private void handleAuthentication(final HttpServletResponse response, final String apiKey, final String accessToken, final String refreshToken) {
        final User user = jwtTokenService.loadUserFromToken(accessToken);
        final FeatherCredentials newCredentials = new FeatherCredentials(apiKey, accessToken.substring(7), refreshToken);
        final Authentication authentication = new FeatherAuthenticationToken(user, newCredentials);
        final Authentication currentAuthentication = authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(currentAuthentication);
        final FeatherCredentials updatedCredentials = (FeatherCredentials) currentAuthentication.getCredentials();
        sendAccessTokenInResponseIfUpdated(response, newCredentials, updatedCredentials);
    }

    @NonNull
    private String getApiKey() {
        final ApiKeyAuthenticationToken currentAuth = (ApiKeyAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return currentAuth.getCredentials();
    }

    private void sendAccessTokenInResponseIfUpdated(final HttpServletResponse response, final FeatherCredentials credentials,
            final FeatherCredentials updatedCredentials) {
        final boolean accessTokenUnchanged = credentials.accessToken().equals(updatedCredentials.accessToken());
        if (!accessTokenUnchanged) {
            response.setHeader(AUTHORIZATION_HEADER, updatedCredentials.accessToken());
        }
    }

}
