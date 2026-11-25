package org.example.marlo_site_helpdesk.service;

import org.example.marlo_site_helpdesk.dto.ProfileDto;
import org.example.marlo_site_helpdesk.models.User;
import org.example.marlo_site_helpdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    private ProfileService profileService;

    private User user;

    @BeforeEach
    void setUp() {

        profileService = new ProfileService(
                userRepository,
                userService
        );

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
    }

    private ProfileDto createDto(String name, String email) {

        ProfileDto dto = new ProfileDto();

        dto.setName(name);
        dto.setEmail(email);

        return dto;
    }

    @Test
    @DisplayName("PROF-01: Получение данных профиля")
    void getProfileSuccess() {

        ProfileDto result = profileService.getProfile(user);

        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        verifyNoInteractions(userRepository, userService);
    }

    @Test
    @DisplayName("PROF-02: Изменение имени")
    void updateNameSuccess() {

        ProfileDto dto = createDto(
                "New Name",
                user.getEmail()
        );

        when(userService.getUser(1L))
                .thenReturn(user);

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(user));

        ProfileService.UpdateResult result =
                profileService.updateProfile(1L, dto);

        assertEquals(
                ProfileService.UpdateResult.SUCCESS,
                result
        );

        assertEquals("New Name", user.getName());
        assertEquals("test@example.com", user.getEmail());

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("PROF-03: Изменение email")
    void updateEmailSuccess() {

        ProfileDto dto = createDto(
                user.getName(),
                "new@example.com"
        );

        when(userService.getUser(1L))
                .thenReturn(user);

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());

        ProfileService.UpdateResult result =
                profileService.updateProfile(1L, dto);

        assertEquals(
                ProfileService.UpdateResult.EMAIL_CHANGED,
                result
        );

        assertEquals("new@example.com", user.getEmail());

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("PROF-04: Email принадлежит другому пользователю")
    void updateEmailAlreadyExists() {

        User anotherUser = new User();

        anotherUser.setId(2L);
        anotherUser.setEmail("another@example.com");

        ProfileDto dto = createDto(
                "New Name",
                anotherUser.getEmail()
        );

        when(userService.getUser(1L))
                .thenReturn(user);

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(anotherUser));

        ProfileService.UpdateResult result =
                profileService.updateProfile(1L, dto);

        assertEquals(
                ProfileService.UpdateResult.EMAIL_EXISTS,
                result
        );

        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("PROF-05: Сохранение собственного email")
    void updateWithOwnEmail() {

        ProfileDto dto = createDto(
                user.getName(),
                user.getEmail()
        );

        when(userService.getUser(1L))
                .thenReturn(user);

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(user));

        ProfileService.UpdateResult result =
                profileService.updateProfile(1L, dto);

        assertEquals(
                ProfileService.UpdateResult.SUCCESS,
                result
        );

        assertEquals("test@example.com", user.getEmail());

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("PROF-06: Удаление лишних пробелов из имени")
    void updateNameWithSpaces() {

        ProfileDto dto = createDto(
                "   New Name   ",
                user.getEmail()
        );

        when(userService.getUser(1L))
                .thenReturn(user);

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(user));

        ProfileService.UpdateResult result =
                profileService.updateProfile(1L, dto);

        assertEquals(
                ProfileService.UpdateResult.SUCCESS,
                result
        );

        assertEquals("New Name", user.getName());

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("PROF-07: Пользователь не существует")
    void updateNonExistingUser() {

        ProfileDto dto = createDto(
                "New Name",
                "new@example.com"
        );

        when(userService.getUser(999L))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED
                        )
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> profileService.updateProfile(999L, dto)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verifyNoInteractions(userRepository);
    }
}