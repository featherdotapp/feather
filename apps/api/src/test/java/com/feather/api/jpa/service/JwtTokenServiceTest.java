package com.feather.api.jpa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.feather.api.jpa.model.User;
import com.feather.api.service.jwt.JwtTokenParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private JwtTokenParser tokenParser;
    @Mock
    private UserService userService;
    @Mock
    private User user;

    @InjectMocks
    private JwtTokenService classUnderTest;

    @Test
    void testLoadUserFromToken() {
        // Arrange
        final String token = "valid.access.token";
        final String bearer = "Bearer " + token;
        final String username = "someEmail@gmail.com";
        when(tokenParser.extractSubject(token)).thenReturn(username);
        when(userService.getUserFromEmail(username)).thenReturn(user);

        // Act
        final User result = classUnderTest.loadUserFromToken(bearer);

        // Assert
        assertThat(result).isEqualTo(user);

    }
}