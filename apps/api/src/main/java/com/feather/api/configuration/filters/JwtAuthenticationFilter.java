package com.feather.api.configuration.filters;

import java.io.IOException;

import com.feather.api.jpa.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A servlet filter that authenticates requests to use JWT.
 * This filter intercepts all incoming requests and checks for a valid JWT in the Authorization header.
 * If a valid JWT is found, it authenticates the user and sets the security context.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * This method is called for each incoming request.
     * It checks for a valid JWT in the Authorization header and authenticates the user if one is found.
     *
     * @param request The incoming request.
     * @param response The outgoing response.
     * @param filterChain The filter chain.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorizedResponse(response, "Invalid or missing JWT Token");
            return;
        }

        try {

            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    writeUnauthorizedResponse(response, "Invalid JWT Token");
                    return;
                }
            } else {
                writeUnauthorizedResponse(response, "Invalid JWT Token");
                return;
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            writeUnauthorizedResponse(response, "Authentication failed: " + e.getMessage());
        }
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + errorMessage + "\"}");
    }
}
