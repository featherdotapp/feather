package com.feather.api.configuration.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class CallbackFilter extends OncePerRequestFilter {

    private static final List<String> CALLBACK_PATHS = List.of(
            "auth/linkedin/callback"
    );
    public static final String CALLBACK_ATTRIBUTE = "IS_CALLBACK_PATH";

    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain
    ) throws IOException, ServletException {
        final String path = request.getServletPath();

        if (CALLBACK_PATHS.stream().anyMatch(path::contains)) {
            //TODO: Check for headers
            request.setAttribute(CALLBACK_ATTRIBUTE, true);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
