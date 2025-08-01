package com.feather.api.jpa.service;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.repository.UserRepository;
import com.feather.api.shared.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service class for managing user-related operations
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to retrieve
     * @return The {@link User} associated with the provided email
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    public User getUserFromEmail(final String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Persists a user entity to the database.
     * If the user already exists (same ID), the existing user will be updated.
     *
     * @param user The {@link User} entity to be saved
     */
    public void saveUser(final User user) {
        userRepository.save(user);
    }

    /**
     * Updates either the access token or refresh token for a specific user.
     *
     * @param user user
     * @param accessToken The new token value to be stored
     * @param tokenType The type of token to update (ACCESS_TOKEN or REFRESH_TOKEN)
     */
    public void updateUserToken(final User user, final String accessToken, final TokenType tokenType) {
        if (tokenType == ACCESS_TOKEN) {
            user.setAccessToken(accessToken);
        } else {
            user.setRefreshToken(accessToken);
        }
        userRepository.save(user);
    }

}
