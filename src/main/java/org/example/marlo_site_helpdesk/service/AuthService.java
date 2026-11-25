package org.example.marlo_site_helpdesk.service;

import org.example.marlo_site_helpdesk.dto.RegisterDto;
import org.example.marlo_site_helpdesk.models.Role;
import org.example.marlo_site_helpdesk.models.User;
import org.example.marlo_site_helpdesk.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public enum RegistrationResult {
        SUCCESS,
        PASSWORD_MISMATCH,
        PASSWORD_TOO_LONG,
        EMAIL_EXISTS
    }

    @Transactional
    public RegistrationResult register(RegisterDto dto) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return RegistrationResult.PASSWORD_MISMATCH;
        }

        if (dto.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            return RegistrationResult.PASSWORD_TOO_LONG;
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return RegistrationResult.EMAIL_EXISTS;
        }

        User user = new User();

        user.setName(dto.getName().trim());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(
                passwordEncoder.encode(dto.getPassword())
        );
        user.setRole(Role.USER);

        userRepository.save(user);

        return RegistrationResult.SUCCESS;
    }
}