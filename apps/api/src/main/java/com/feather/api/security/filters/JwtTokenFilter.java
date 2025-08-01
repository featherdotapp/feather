package com.feather.api.security.filters;

import static com.feather.api.shared.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.feather.api.shared.AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME;

import java.io.IOException;
import java.util.Optional;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.security.exception_handling.FeatherAuthenticationEntryPoint;
import com.feather.api.security.exception_handling.exception.JwtAuthenticationException;
import com.feather.api.security.tokens.FeatherAuthenticationToken;
import com.feather.api.security.tokens.credentials.FeatherCredentials;
import com.feather.api.service.CookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    @Value("${security.jwt.access-expiration-time}")
    private String accessTokenExpirationTime;

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
        try {
            final Optional<Cookie> refreshTokenCookie = cookieService.findCookie(request.getCookies(), REFRESH_TOKEN_COOKIE_NAME);
            final Optional<Cookie> accessTokenCookie = cookieService.findCookie(request.getCookies(), ACCESS_TOKEN_COOKIE_NAME);
            final String apiKey = getApiKey();
            if (refreshTokenCookie.isPresent() && accessTokenCookie.isPresent() && !apiKey.isEmpty()) {
                final String refreshToken = refreshTokenCookie.get().getValue();
                final String accessToken = accessTokenCookie.get().getValue();
                if (!refreshToken.isEmpty() && !accessToken.isEmpty()) {
                    handleAuthentication(response, apiKey, accessToken, refreshToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (final BadCredentialsException e) {
            if (request.getRequestURI().equals("/auth/linkedin/loginUrl")) {
                filterChain.doFilter(request, response);
            } else {
                throw new BadCredentialsException("Refresh token cookie not found: " + e.getMessage());
            }
        } catch (final JwtAuthenticationException | UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, e);
        }
    }

    private void handleAuthentication(final HttpServletResponse response, final String apiKey, final String accessToken, final String refreshToken) {
        final User user = jwtTokenService.loadUserFromToken(accessToken);
        final FeatherCredentials newCredentials = new FeatherCredentials(apiKey, accessToken, refreshToken);
        final Authentication authentication = new FeatherAuthenticationToken(user, newCredentials);
        final Authentication currentAuthentication = authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(currentAuthentication);
        final FeatherCredentials updatedCredentials = (FeatherCredentials) currentAuthentication.getCredentials();
        sendAccessTokenInResponseIfUpdated(response, newCredentials, updatedCredentials);
    }

    @NonNull
    private String getApiKey() {
        final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth != null) {
            return currentAuth.getCredentials().toString();
        }
        return "";
    }

    private void sendAccessTokenInResponseIfUpdated(final HttpServletResponse response, final FeatherCredentials credentials,
            final FeatherCredentials updatedCredentials) {
        final boolean accessTokenUnchanged = credentials.accessToken().equals(updatedCredentials.accessToken());
        if (!accessTokenUnchanged) {
            final Cookie cookie = cookieService.createCookie(ACCESS_TOKEN_COOKIE_NAME, updatedCredentials.accessToken(), accessTokenExpirationTime);
            response.addCookie(cookie);
        }
    }

}
