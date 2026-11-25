package org.example.marlo_site_helpdesk.service;

import org.example.marlo_site_helpdesk.dto.ProfileDto;
import org.example.marlo_site_helpdesk.models.User;
import org.example.marlo_site_helpdesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final UserService userService;

    public ProfileService(
            UserRepository userRepository,
            UserService userService) {

        this.userRepository = userRepository;
        this.userService = userService;
    }

    public enum UpdateResult {
        SUCCESS,
        EMAIL_CHANGED,
        EMAIL_EXISTS
    }

    public ProfileDto getProfile(User user) {

        ProfileDto dto = new ProfileDto();

        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        return dto;
    }

    @Transactional
    public UpdateResult updateProfile(
            Long userId,
            ProfileDto dto) {

        User user = userService.getUser(userId);

        Optional<User> existingUser =
                userRepository.findByEmail(dto.getEmail());

        if (existingUser.isPresent()
                && !existingUser.get().getId().equals(user.getId())) {

            return UpdateResult.EMAIL_EXISTS;
        }

        boolean emailChanged =
                !user.getEmail().equals(dto.getEmail());

        user.setName(dto.getName().trim());
        user.setEmail(dto.getEmail());

        userRepository.save(user);

        if (emailChanged) {
            return UpdateResult.EMAIL_CHANGED;
        }

        return UpdateResult.SUCCESS;
    }
}