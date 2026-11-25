package org.example.marlo_site_helpdesk.service;

import org.example.marlo_site_helpdesk.dto.RegisterDto;
import org.example.marlo_site_helpdesk.models.Role;
import org.example.marlo_site_helpdesk.models.User;
import org.example.marlo_site_helpdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder
        );
    }

    private RegisterDto createDto() {

        RegisterDto dto = new RegisterDto();

        dto.setName("  Test User  ");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");

        return dto;
    }

    @Test
    @DisplayName("AUTH-01: Успешная регистрация")
    void registerSuccess() {

        RegisterDto dto = createDto();

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(dto.getPassword()))
                .thenReturn("encoded-password");

        AuthService.RegistrationResult result =
                authService.register(dto);

        assertEquals(
                AuthService.RegistrationResult.SUCCESS,
                result
        );

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertEquals("Test User", savedUser.getName());
        assertEquals(dto.getEmail(), savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPasswordHash());
        assertEquals(Role.USER, savedUser.getRole());

        assertNotEquals(
                dto.getPassword(),
                savedUser.getPasswordHash()
        );

        verify(passwordEncoder).encode(dto.getPassword());
    }

    @Test
    @DisplayName("AUTH-02: Пароли не совпадают")
    void registerPasswordMismatch() {

        RegisterDto dto = createDto();

        dto.setConfirmPassword("anotherPassword");

        AuthService.RegistrationResult result =
                authService.register(dto);

        assertEquals(
                AuthService.RegistrationResult.PASSWORD_MISMATCH,
                result
        );

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("AUTH-03: Email уже используется")
    void registerEmailExists() {

        RegisterDto dto = createDto();

        User existingUser = new User();
        existingUser.setEmail(dto.getEmail());

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(existingUser));

        AuthService.RegistrationResult result =
                authService.register(dto);

        assertEquals(
                AuthService.RegistrationResult.EMAIL_EXISTS,
                result
        );

        verify(userRepository, never()).save(any(User.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("AUTH-04: Пароль длиннее 72 байт")
    void registerPasswordTooLong() {

        RegisterDto dto = createDto();

        String password = "a".repeat(73);

        dto.setPassword(password);
        dto.setConfirmPassword(password);

        AuthService.RegistrationResult result =
                authService.register(dto);

        assertEquals(
                AuthService.RegistrationResult.PASSWORD_TOO_LONG,
                result
        );

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("AUTH-05: Пароль ровно 72 байта")
    void registerPasswordMaximumLength() {

        RegisterDto dto = createDto();

        String password = "a".repeat(72);

        dto.setPassword(password);
        dto.setConfirmPassword(password);

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(password))
                .thenReturn("encoded-password");

        AuthService.RegistrationResult result =
                authService.register(dto);

        assertEquals(
                AuthService.RegistrationResult.SUCCESS,
                result
        );

        verify(passwordEncoder).encode(password);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("AUTH-06: Превышение лимита UTF-8")
    void registerUnicodePasswordTooLong() {

        RegisterDto dto = createDto();

        String password = "я".repeat(37);

        dto.setPassword(password);
        dto.setConfirmPassword(password);

        AuthService.RegistrationResult result =
                authService.register(dto);

        assertEquals(
                AuthService.RegistrationResult.PASSWORD_TOO_LONG,
                result
        );

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("AUTH-07: Хеширование пароля BCrypt")
    void registerWithRealPasswordEncoder() {

        RegisterDto dto = createDto();

        PasswordEncoder realEncoder =
                new BCryptPasswordEncoder();

        AuthService realAuthService =
                new AuthService(userRepository, realEncoder);

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());

        AuthService.RegistrationResult result =
                realAuthService.register(dto);

        assertEquals(
                AuthService.RegistrationResult.SUCCESS,
                result
        );

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertNotEquals(
                dto.getPassword(),
                savedUser.getPasswordHash()
        );

        assertTrue(
                realEncoder.matches(
                        dto.getPassword(),
                        savedUser.getPasswordHash()
                )
        );

        assertFalse(
                realEncoder.matches(
                        "wrongPassword",
                        savedUser.getPasswordHash()
                )
        );
    }
}