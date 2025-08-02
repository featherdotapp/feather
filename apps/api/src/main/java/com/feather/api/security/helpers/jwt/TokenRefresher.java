package com.feather.api.security.helpers.jwt;

import static com.feather.api.shared.TokenType.ACCESS_TOKEN;
import static com.feather.api.shared.TokenType.REFRESH_TOKEN;

import com.feather.api.jpa.model.User;
import com.feather.api.jpa.service.UserService;
import com.feather.api.service.jwt.JwtTokenBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component responsible for refreshing JWT access and refresh tokens when they are close to expiring.
 */
@Component
@RequiredArgsConstructor
public class TokenRefresher {

    private final UserService userService;
    private final JwtTokenValidator jwtTokenValidator;
    private final JwtTokenBuilder jwtTokenBuilder;

    /**
     * Refreshes the access and/or refresh token for the given user if either is nearing expiration.
     *
     * @param accessToken the current access token
     * @param refreshToken the current refresh token
     * @param user the associated user
     */
    public User refreshTokens(final String accessToken, final String refreshToken, final User user) {
        if (jwtTokenValidator.shouldUpdateAccessToken(accessToken, refreshToken, user)) {
            final String newAccessToken = jwtTokenBuilder.buildToken(user, ACCESS_TOKEN);
            userService.updateUserToken(user, newAccessToken, ACCESS_TOKEN);
        }
        if (jwtTokenValidator.shouldUpdateRefreshToken(refreshToken)) {
            final String newRefreshToken = jwtTokenBuilder.buildToken(user, REFRESH_TOKEN);
            userService.updateUserToken(user, newRefreshToken, REFRESH_TOKEN);
        }
        return userService.getUserFromEmail(user.getEmail());
    }
}
