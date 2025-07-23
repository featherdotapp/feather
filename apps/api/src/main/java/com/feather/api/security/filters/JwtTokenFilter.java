package com.feather.api.security.filters;

import static com.feather.api.shared.AuthenticationConstants.AUTHORIZATION_HEADER;
import static com.feather.api.shared.AuthenticationConstants.BEARER_PREFIX;
import static com.feather.api.shared.AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME;

import java.io.IOException;
import java.util.Optional;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.JwtTokenService;
import com.feather.api.jpa.service.UserService;
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
    private final UserService userService;

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

        if (refreshTokenCookie.isPresent() && accessToken != null && apiKey != null) {
            final String refreshToken = refreshTokenCookie.get().getValue();
            if (!refreshToken.isEmpty() && hasBearerPrefix(accessToken)) {
                final User user = loadUserFromToken(accessToken);
                final FeatherCredentials credentials = new FeatherCredentials(apiKey, accessToken.substring(7), refreshToken);
                final Authentication auth = new FeatherAuthenticationToken(user, credentials);
                final Authentication applicationAuthentication = authenticationManager.authenticate(auth);
                SecurityContextHolder.getContext().setAuthentication(applicationAuthentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getApiKey() {
        final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        return currentAuth != null
                && currentAuth.getCredentials() instanceof final String castedCredentials
                ? castedCredentials
                : null;
    }

    private User loadUserFromToken(final String accessToken) throws UsernameNotFoundException {
        final String token = accessToken.substring(BEARER_PREFIX.length());
        final String userName = jwtTokenService.extractUsername(token);
        return userService.getUserFromEmail(userName);
    }

}
