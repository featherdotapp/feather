package com.feather.api.jpa.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        // Arrange
        user = new User();
        user.setEmail("test@example.com");
        user.setAccessToken("access-token-123");
        user.setRefreshToken("refresh-token-456");
        user.setUserRoles(List.of(Role.DEFAULT_USER));
        user.setOAuthProviders(Set.of(User.DEFAULT_OAUTH_PROVIDER));
    }

    @Test
    void testDefaultValues() {
        // Arrange
        User newUser = new User();

        // Assert
        assertEquals("<PASSWORD>", newUser.getPassword(), "Default password should be set");
        assertEquals(List.of(Role.DEFAULT_USER), newUser.getUserRoles(), "Default roles should be set");
        assertEquals(Set.of(User.DEFAULT_OAUTH_PROVIDER), newUser.getOAuthProviders(), "Default OAuth provider should be LinkedIn");
        assertEquals("LinkedIn", User.DEFAULT_OAUTH_PROVIDER, "Default OAuth provider constant should be LinkedIn");
    }

    @Test
    void testGetAuthorities() {
        // Arrange
        user.setUserRoles(List.of(Role.DEFAULT_USER, Role.PREMIUM_USER));

        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities, "Authorities should not be null");
        assertEquals(2, authorities.size(), "Should have two authorities");
        assertTrue(authorities.contains(new SimpleGrantedAuthority("DEFAULT_USER")), "Should contain DEFAULT_USER authority");
        assertTrue(authorities.contains(new SimpleGrantedAuthority("PREMIUM_USER")), "Should contain PREMIUM_USER authority");
    }

    @Test
    void testGetUsername() {
        // Act & Assert
        assertEquals("test@example.com", user.getUsername(), "Username should return email");
    }

    @Test
    void testGetPassword() {
        // Act & Assert
        assertEquals("<PASSWORD>", user.getPassword(), "Password should return the default password value");
    }

    @Test
    void testSetAccessToken() {
        // Arrange
        String newToken = "new-access-token";

        // Act
        user.setAccessToken(newToken);

        // Assert
        assertEquals(newToken, user.getAccessToken(), "Access token should be updated");
    }

    @Test
    void testSetRefreshToken() {
        // Arrange
        String newToken = "new-refresh-token";

        // Act
        user.setRefreshToken(newToken);

        // Assert
        assertEquals(newToken, user.getRefreshToken(), "Refresh token should be updated");
    }

    @Test
    void testSetEmail() {
        // Arrange
        String newEmail = "new@example.com";

        // Act
        user.setEmail(newEmail);

        // Assert
        assertEquals(newEmail, user.getEmail(), "Email should be updated");
        assertEquals(newEmail, user.getUsername(), "Username should reflect updated email");
    }

    @Test
    void testSetUserRoles() {
        // Arrange
        List<Role> newRoles = List.of(Role.PREMIUM_USER);

        // Act
        user.setUserRoles(newRoles);

        // Assert
        assertEquals(newRoles, user.getUserRoles(), "User roles should be updated");
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(1, authorities.size(), "Should have one authority");
        assertTrue(authorities.contains(new SimpleGrantedAuthority("PREMIUM_USER")), "Should contain PREMIUM_USER authority");
    }

    @Test
    void testSetOAuthProviders() {
        // Arrange
        Set<String> newProviders = Set.of("Google", "Facebook");

        // Act
        user.setOAuthProviders(newProviders);

        // Assert
        assertEquals(newProviders, user.getOAuthProviders(), "OAuth providers should be updated");
    }

    @Test
    void testDefaultOAuthProvider() {
        // Assert
        assertEquals("LinkedIn", User.DEFAULT_OAUTH_PROVIDER, "Default OAuth provider should be LinkedIn");
    }
}
