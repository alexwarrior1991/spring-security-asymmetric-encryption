package com.alejandro.app.user.impl;

import com.alejandro.app.exception.BusinessException;
import com.alejandro.app.user.User;
import com.alejandro.app.user.UserMapper;
import com.alejandro.app.user.UserRepository;
import com.alejandro.app.user.UserService;
import com.alejandro.app.user.request.ChangePasswordRequest;
import com.alejandro.app.user.request.ProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Predicate;

import static com.alejandro.app.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with userEmail : " + userEmail));
    }

    @Override
    public void updateProfileInfo(ProfileUpdateRequest request, String userId) {
        final User savedUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        userMapper.mergeUserInfo(savedUser, request);
        userRepository.save(savedUser);
    }

    @Override
    public void changePassword(final ChangePasswordRequest request, String userId) {

        // Predicate para validar que las contraseñas coinciden
        Predicate<ChangePasswordRequest> passwordsMatch = req ->
                req.getNewPassword().equals(req.getConfirmNewPassword());

        // Verificación funcional de las contraseñas
        if (!passwordsMatch.test(request)) {
            throw new BusinessException(CHANGE_PASSWORD_MISMATCH);
        }

        // Obtener el usuario o lanzar excepción si no existe
        User savedUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        // Predicate para validar que la contraseña actual es válida
        Predicate<String> currentPasswordMatches = currentPassword ->
                passwordEncoder.matches(currentPassword, savedUser.getPassword());

        // Verificación funcional de la contraseña actual
        if (!currentPasswordMatches.test(request.getCurrentPassword())) {
            throw new BusinessException(INVALID_CURRENT_PASSWORD);
        }

        // Codificar la nueva contraseña y guardar el usuario actualizado
        Optional.of(request.getNewPassword())
                .map(passwordEncoder::encode) // Codificar la nueva contraseña
                .ifPresent(encodedPassword -> {
                    savedUser.setPassword(encodedPassword);
                    userRepository.save(savedUser); // Guardar los cambios
                });

    }

    @Override
    public void deactivateAccount(final String userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        Optional.of(user)
                .filter(u -> !u.isEnabled())
                .ifPresent(u -> {
                    throw new BusinessException(ACCOUNT_ALREADY_DEACTIVATED);
                });

        Optional.of(user)
                .map(u -> {
                    u.setEnabled(false);
                    return userRepository.save(u);
                });
    }

    @Override
    public void reactivateAccount(final String userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        Optional.of(user)
                .filter(User::isEnabled)
                .ifPresent(u -> {
                    throw new BusinessException(ACCOUNT_ALREADY_DEACTIVATED);
                });

        Optional.of(user)
                .map(u -> {
                    u.setEnabled(true);
                    return userRepository.save(u);
                });
    }

    @Override
    public void deleteAccount(String userId) {
        // this method need the rest of the entities
        // the logic is just to schedule a profile for deletion
        // and then a scheduled job will pick up the profiles and delete everything
    }
}
