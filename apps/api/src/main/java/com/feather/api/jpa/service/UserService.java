package com.feather.api.jpa.service;

import java.util.Objects;
import java.util.Optional;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves a user by their email.
     *
     * @param email The email of the user.
     * @return The user associated with the email, or null if not found.
     */
    public User getUserFromEmail(final String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Checks if the provided token matches the token saved on the db with the current user
     *
     * @param token jwt token
     * @param email user email
     * @return true if the jwt matches, false otherwise
     */
    public boolean isJwtValidForUser(final String token, final String email) {
        final Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            return Objects.equals(user.getToken(), token);
        }
        return false;
    }
}
