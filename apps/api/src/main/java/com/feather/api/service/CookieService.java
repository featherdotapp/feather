package com.feather.api.service;

import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

/**
 * Helper class related to {@link Cookie Cookies}
 */
@Service
public class CookieService {

    /**
     * Finds a cookie with the specified name in an array of cookies.
     *
     * @param cookies The array of cookies to search through
     * @param cookieName The name of the cookie to find
     * @return An Optional containing the found cookie, or an empty Optional if no matching cookie exists
     */
    public Optional<Cookie> findCookie(final Cookie[] cookies, final String cookieName) {
        if (cookies == null) {
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
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(expiry);
        return refreshTokenCookie;
    }

}
