package org.example.marlo_site_helpdesk.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.marlo_site_helpdesk.models.User;
import org.example.marlo_site_helpdesk.repository.UserRepository;
import org.example.marlo_site_helpdesk.dto.ProfileDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String profile(Authentication auth, Model model) {

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow();

        ProfileDto dto = new ProfileDto();

        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        model.addAttribute("profile", dto);
        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping
    public String updateProfile(
            @Valid @ModelAttribute("profile") ProfileDto dto,
            BindingResult bindingResult,
            Authentication auth,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow();

        model.addAttribute("user", user);

        if (bindingResult.hasErrors()) {
            return "profile";
        }

        Optional<User> existingUser =
                userRepository.findByEmail(dto.getEmail());

        if (existingUser.isPresent()
                && !existingUser.get().getId().equals(user.getId())) {

            bindingResult.rejectValue(
                    "email",
                    "email.exists",
                    "Эта почта уже используется"
            );

            return "profile";
        }

        boolean emailChanged =
                !user.getEmail().equals(dto.getEmail());

        user.setName(dto.getName().trim());
        user.setEmail(dto.getEmail());

        userRepository.save(user);

        if (emailChanged) {

            new SecurityContextLogoutHandler()
                    .logout(request, response, auth);

            return "redirect:/login?emailChanged";
        }

        return "redirect:/profile?success";
    }
}