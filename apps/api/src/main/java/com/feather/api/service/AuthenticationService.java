package com.feather.api.service;

import com.feather.api.jpa.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtTokenService jwtTokenService;

    // TODO: Create methods on AuthenticationService to login and register
}
