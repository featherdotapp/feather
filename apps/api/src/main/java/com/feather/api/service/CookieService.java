package com.feather.api.service;

import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

/**
 * Helper class related to {@link Cookie Cookies}
 */
@Service
public class CookieService {

    private final boolean secureCookies;

    /**
     * Constructs a CookieService and determines if cookies should be secure based on the active Spring profile.
     *
     * @param environment the Spring Environment used to check active profiles
     * (if 'dev' is active, cookies are not secure; otherwise, cookies are secure)
     */
    public CookieService(final Environment environment) {
        final String[] profiles = environment.getActiveProfiles();
        boolean isDev = false;
        for (final String profile : profiles) {
            if ("dev".equalsIgnoreCase(profile)) {
                isDev = true;
                break;
            }
        }
        this.secureCookies = !isDev;
    }

    /**
     * Finds a cookie with the specified name in an array of cookies.
     *
     * @param cookies The array of cookies to search through
     * @param cookieName The name of the cookie to find
     * @return An Optional containing the found cookie, or an empty Optional if no matching cookie exists
     */
    public Optional<Cookie> findCookie(final Cookie[] cookies, final String cookieName) {
        if (cookies == null || cookies.length == 0) {
            throw new BadCredentialsException(cookieName + " cookie not found");
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst();
    }

    /**
     * Creates a new HTTP cookie with the specified name, value, and expiration time.
     *
     * @param cookieName the name of the cookie
     * @param cookieValue the value of the cookie
     * @param expiry the expiration time of the cookie in seconds
     * @return the created Cookie object
     */
    public Cookie createCookie(final String cookieName, final String cookieValue, final int expiry) {
        final Cookie refreshTokenCookie = new Cookie(cookieName, cookieValue);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(secureCookies);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(expiry);
        return refreshTokenCookie;
    }

}
