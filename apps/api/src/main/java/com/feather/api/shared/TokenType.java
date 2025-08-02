package com.feather.api.shared;

import lombok.Getter;

/**
 * JWT Token Types
 */
@Getter
public enum TokenType {
    ACCESS_TOKEN("access_token_cookie"),
    REFRESH_TOKEN("refresh_token_cookie");

    private final String cookieName;

    TokenType(String cookieName) {
        this.cookieName = cookieName;
    }

}
