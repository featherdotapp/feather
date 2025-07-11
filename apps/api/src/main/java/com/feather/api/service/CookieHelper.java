package com.feather.api.service;

import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;

/**
 * Helper class related to {@link Cookie Cookies}
 */
@Service
public class CookieHelper {

    /**
     * Finds a cookie with the specified name in an array of cookies.
     *
     * @param cookies    The array of cookies to search through
     * @param cookieName The name of the cookie to find
     * @return An Optional containing the found cookie, or an empty Optional if no matching cookie exists
     */
    public Optional<Cookie> findCookie(final Cookie[] cookies, final String cookieName) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst();
    }

}
