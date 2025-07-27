package com.alejandro.app.user;

import com.alejandro.app.auth.request.RegistrationRequest;
import com.alejandro.app.user.request.ProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public User toUser(final RegistrationRequest request) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .locked(false)
                .credentialsExpired(false)
                .emailVerified(false)
                .phoneVerified(false)
                .build();
    }

    public void mergeUserInfo(final User user, final ProfileUpdateRequest request) {
        Optional.ofNullable(request.getFirstName())
                .filter(StringUtils::isNotBlank)
                .filter(firstName -> !firstName.equals(user.getFirstName()))
                .ifPresent(user::setFirstName);

        Optional.ofNullable(request.getLastName())
                .filter(StringUtils::isNotBlank)
                .filter(lastName -> !lastName.equals(user.getLastName()))
                .ifPresent(user::setLastName);

        Optional.ofNullable(request.getDateOfBirth())
                .filter(dob -> !dob.equals(user.getDateOfBirth()))
                .ifPresent(user::setDateOfBirth);
    }
}
