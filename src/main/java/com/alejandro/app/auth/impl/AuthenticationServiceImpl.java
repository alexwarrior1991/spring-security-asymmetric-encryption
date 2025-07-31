package com.alejandro.app.auth.impl;

import com.alejandro.app.auth.AuthenticationService;
import com.alejandro.app.auth.request.AuthenticationRequest;
import com.alejandro.app.auth.request.RefreshRequest;
import com.alejandro.app.auth.request.RegistrationRequest;
import com.alejandro.app.auth.response.AuthenticationResponse;
import com.alejandro.app.exception.BusinessException;
import com.alejandro.app.exception.ErrorCode;
import com.alejandro.app.role.Role;
import com.alejandro.app.role.RoleRepository;
import com.alejandro.app.security.JwtService;
import com.alejandro.app.user.User;
import com.alejandro.app.user.UserMapper;
import com.alejandro.app.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.alejandro.app.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

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
    @Transactional
    public void register(RegistrationRequest request) {
        checkUserEmail(request.getEmail());
        checkPhoneNumber(request.getPhoneNumber());
        checkPasswords(request.getPassword(), request.getConfirmPassword());

        final Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new EntityNotFoundException("Role user does not exist"));


        final User user = userMapper.toUser(request);
        user.addRole(userRole);
        log.debug("Saving user {} with role {}", user, userRole);
        userRepository.save(user);
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        final String newAccessToken = jwtService.refreshAccessToken(request.getRefreshToken());
        final String tokenType = "Bearer";

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType(tokenType)
                .build();
    }

    private void checkUserEmail(final String email) {
        Optional.of(email)
                .filter(userRepository::existsByEmailIgnoreCase)
                .ifPresent(e -> {
                    throw new BusinessException(EMAIL_ALREADY_EXISTS);
                });
    }

    private void checkPasswords(final String password, final String confirmPassword) {
        Optional.ofNullable(password)
                .filter(p -> p.equals(confirmPassword))
                .orElseThrow(() -> new BusinessException(PASSWORD_MISMATCH));
    }

    private void checkPhoneNumber(final String phoneNumber) {
        Optional.of(phoneNumber)
                .filter(userRepository::existsByPhoneNumber)
                .ifPresent(p -> {
                    throw new BusinessException(PHONE_ALREADY_EXISTS);
                });
    }
}
