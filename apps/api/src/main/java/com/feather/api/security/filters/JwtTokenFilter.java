package com.feather.api.security.filters;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;
import static com.feather.api.shared.TokenType.REFRESH_TOKEN;

import java.io.IOException;
import java.util.Optional;

import com.feather.api.security.helpers.AuthenticationHandler;
import com.feather.api.service.CookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final CookieService cookieService;
    private final AuthenticationHandler authenticationHandler;

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
            @NonNull final FilterChain filterChain) throws ServletException, IOException {
        try {
            final Optional<Cookie> refreshTokenCookie = cookieService.findCookie(request.getCookies(), REFRESH_TOKEN.getCookieName());
            final Optional<Cookie> accessTokenCookie = cookieService.findCookie(request.getCookies(), ACCESS_TOKEN.getCookieName());
            final String apiKey = getApiKey();
            if (refreshTokenCookie.isPresent() && accessTokenCookie.isPresent() && !apiKey.isEmpty()) {
                final String refreshToken = refreshTokenCookie.get().getValue();
                final String accessToken = accessTokenCookie.get().getValue();
                if (!refreshToken.isEmpty() && !accessToken.isEmpty()) {
                    authenticationHandler.handleAuthentication(request, response, apiKey, accessToken, refreshToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (final BadCredentialsException e) {
            if (request.getRequestURI().equals("/auth/linkedin/loginUrl")) {
                filterChain.doFilter(request, response);
            } else {
                throw new BadCredentialsException("Refresh token cookie not found: " + e.getMessage());
            }
        }
    }

    private String getApiKey() {
        final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth != null) {
            return currentAuth.getCredentials().toString();
        }
        return "";
    }

}
