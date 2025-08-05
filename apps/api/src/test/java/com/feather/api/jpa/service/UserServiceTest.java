package com.feather.api.jpa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.repository.UserRepository;
import com.feather.api.shared.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String USER_EMAIL = "test@example.com";
    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String REFRESH_TOKEN = "test-refresh-token";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail(USER_EMAIL);
    }

    @Test
    void getUserFromEmail_shouldReturnUser_whenUserExists() {
        // Arrange
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));

        // Act
        final User result = userService.getUserFromEmail(USER_EMAIL);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(USER_EMAIL);
        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
    }

    @Test
    void getUserFromEmail_shouldThrowException_whenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserFromEmail(USER_EMAIL))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
    }

    @Test
    void saveUser_shouldCallRepositorySave() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.saveUser(testUser);

        // Assert
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateUserToken_shouldUpdateAccessToken_whenTokenTypeIsAccessToken() {
        // Act
        userService.updateUserToken(testUser, ACCESS_TOKEN, TokenType.ACCESS_TOKEN);

        // Assert
        assertThat(testUser.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateUserToken_shouldUpdateRefreshToken_whenTokenTypeIsRefreshToken() {
        // Act
        userService.updateUserToken(testUser, REFRESH_TOKEN, TokenType.REFRESH_TOKEN);

        // Assert
        assertThat(testUser.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        verify(userRepository, times(1)).save(testUser);
    }
}
