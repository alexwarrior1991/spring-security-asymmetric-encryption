package com.alejandro.app.auth.impl;

import com.alejandro.app.auth.AuthenticationService;
import com.alejandro.app.auth.request.AuthenticationRequest;
import com.alejandro.app.auth.request.RefreshRequest;
import com.alejandro.app.auth.request.RegistrationRequest;
import com.alejandro.app.auth.response.AuthenticationResponse;
import com.alejandro.app.security.JwtService;
import com.alejandro.app.user.User;
import com.alejandro.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;


    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        final Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        final User user = (User) auth.getPrincipal();
        final String token = jwtService.generateAccessToken(user.getUsername());
        final String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        final String tokenType = "Bearer";

        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .build();
    }

    @Override
    public void register(RegistrationRequest request) {

    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        return null;
    }
}
