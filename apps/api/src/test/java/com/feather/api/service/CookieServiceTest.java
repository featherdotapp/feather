package com.feather.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class CookieServiceTest {

    @Mock
    private Environment environment;

    private CookieService classUnderTest;

    @Nested
    class ProdTest {

        @BeforeEach
        void setUp() {
            when(environment.getActiveProfiles()).thenReturn(new String[] { "prod" });
            classUnderTest = new CookieService(environment);
        }

        @Test
        void testCreateCookie_secureTrueWhenProdProfile() {
            // Act
            final Cookie cookie = classUnderTest.createCookie("test", "val", 100);
            // Assert
            assertThat(cookie.getSecure()).isTrue();
        }
    }

    @Nested
    class DevTest {

        @BeforeEach
        void setUp() {
            when(environment.getActiveProfiles()).thenReturn(new String[] { "dev" });
            classUnderTest = new CookieService(environment);
        }

        @Test
        void testFindCookie() {
            // Arrange
            final String targetCookieName = "sessionId";
            final String targetCookieValue = "abc123";
            final Cookie[] cookies = {
                    new Cookie("otherCookie", "value1"),
                    new Cookie(targetCookieName, targetCookieValue),
                    new Cookie("anotherCookie", "value2")
            };

            // Act
            final Optional<Cookie> result = classUnderTest.findCookie(cookies, targetCookieName);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo(targetCookieName);
            assertThat(result.get().getValue()).isEqualTo(targetCookieValue);
        }

        @Test
        void testFindCookie_notFound() {
            // Arrange
            final String targetCookieName = "nonExistentCookie";
            final Cookie[] cookies = {
                    new Cookie("cookie1", "value1"),
                    new Cookie("cookie2", "value2")
            };

            // Act
            final Optional<Cookie> result = classUnderTest.findCookie(cookies, targetCookieName);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        void testFindCookie_emptyArray() {
            // Arrange
            final String targetCookieName = "anyCookie";
            final Cookie[] cookies = {};

            // Act & Assert
            assertThrows(BadCredentialsException.class, () -> classUnderTest.findCookie(cookies, targetCookieName));

        }

        @Test
        void testFindCookie_nullArray() {
            // Arrange
            final String targetCookieName = "anyCookie";

            // Act & Assert
            final BadCredentialsException exception = assertThrows(
                    BadCredentialsException.class,
                    () -> classUnderTest.findCookie(null, targetCookieName)
            );
            assertThat(exception.getMessage()).isEqualTo(targetCookieName + " cookie not found");
        }

        @Test
        void testCreateCookie() {
            // Arrange
            classUnderTest = new CookieService(environment);
            final String cookieName = "refreshToken";
            final String cookieValue = "jwt123token";
            final int expiry = 3600;

            // Act
            final Cookie result = classUnderTest.createCookie(cookieName, cookieValue, expiry);

            // Assert
            assertThat(result.getName()).isEqualTo(cookieName);
            assertThat(result.getValue()).isEqualTo(cookieValue);
            assertThat(result.getMaxAge()).isEqualTo(expiry);
            // Test hardcoded constants
            assertThat(result.isHttpOnly()).isTrue();
            assertThat(result.getSecure()).isFalse();
            assertThat(result.getPath()).isEqualTo("/");
        }

    }
}