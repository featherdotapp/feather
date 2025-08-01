package com.feather.api.security.filters;

import java.io.IOException;

import com.feather.api.security.exception_handling.FeatherAuthenticationEntryPoint;
import com.feather.api.security.exception_handling.exception.ApiKeyAuthenticationException;
import com.feather.api.security.tokens.ApiKeyAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter for API key-based authentication.
 * <p>
 * Extracts the API key from the request header and authenticates it using the AuthenticationManager.
 * If the API key is valid, sets the authentication in the SecurityContext.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final FeatherAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * Performs API key authentication for each request.
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
            final String apiKey = request.getHeader("X-API-KEY");
            if (apiKey != null) {
                final Authentication auth = new ApiKeyAuthenticationToken(apiKey);
                SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(auth));
            }
            filterChain.doFilter(request, response);
        } catch (final ApiKeyAuthenticationException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, e);
        }
    }

}

