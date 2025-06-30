package com.feather.api.configuration.filters;

import java.io.IOException;

import com.feather.api.configuration.filters.utils.AuthResult;
import com.feather.api.configuration.filters.utils.JwtAuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter for authenticating requests using JWT tokens.
 * This filter intercepts incoming HTTP requests, validates the JWT token in the Authorization header,
 * and sets the security context for authenticated users. If the token is invalid or expired, it handles
 * the response accordingly.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationService jwtAuthenticationService;

     /**
     * Processes each incoming HTTP request to validate the JWT token.
     * If the token is valid, the user is authenticated and the request proceeds.
     * If the token is invalid or expired, an appropriate response is sent back to the client.
     *
     * @param request The incoming HTTP request.
     * @param response The outgoing HTTP response.
     * @param filterChain The filter chain to pass the request to the next filter.
     * @throws IOException If an I/O error occurs during processing.
     * @throws ServletException If a servlet error occurs during processing.
     */
    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain
    ) throws IOException, ServletException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorizedResponse(response, "Invalid or missing JWT Token");
            return;
        }

        final String accessToken = authHeader.substring(7);
        final AuthResult result = jwtAuthenticationService.authenticate(request, accessToken);
        switch (result.status()) {
            case SUCCESS -> {
                setAuthenticationToken(request, result.userDetails());
                filterChain.doFilter(request, response);
            }
            case REFRESHED -> {
                response.setContentType("application/json");
                response.getWriter().write("{\"accessToken\": \"" + result.newAccessToken() + "\"}");
                response.setStatus(HttpServletResponse.SC_OK);
            }
            case INVALID -> writeUnauthorizedResponse(response, result.errorMessage());
        }
    }

    private void setAuthenticationToken(final HttpServletRequest request, final UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void writeUnauthorizedResponse(final HttpServletResponse response, final String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + errorMessage + "\"}");
    }
}
