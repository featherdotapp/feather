package com.feather.api.jpa.service;

import com.feather.api.jpa.model.User;
import com.feather.api.service.jwt.JwtTokenParser;
import com.feather.api.shared.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service in charge of handling JWT Token logic
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtTokenParser jwtTokenParser;
    private final UserService userService;

    /**
     * Retrieves the UserDetails from a {@link TokenType#ACCESS_TOKEN}
     *
     * @param accessToken the string access token
     * @return the User implementation of UserDetails
     */
    public User loadUserFromToken(final String accessToken) throws UsernameNotFoundException {
        final String userName = jwtTokenParser.extractSubject(accessToken);
        return userService.getUserFromEmail(userName);
    }

}
