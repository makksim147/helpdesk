package org.example.marlo_site_helpdesk.controller;

import jakarta.validation.Valid;
import org.example.marlo_site_helpdesk.models.Role;
import org.example.marlo_site_helpdesk.models.User;
import org.example.marlo_site_helpdesk.repository.UserRepository;
import org.example.marlo_site_helpdesk.dto.RegisterDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new RegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") RegisterDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Пароли не совпадают"
            );

            return "register";
        }

        if (dto.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            bindingResult.rejectValue(
                    "password",
                    "password.length",
                    "Пароль слишком длинный"
            );

            return "register";
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            bindingResult.rejectValue(
                    "email",
                    "email.exists",
                    "Пользователь с такой почтой уже существует"
            );

            return "register";
        }

        User user = new User();

        user.setName(dto.getName().trim());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(
                passwordEncoder.encode(dto.getPassword())
        );
        user.setRole(Role.USER);

        userRepository.save(user);

        return "redirect:/login?registered";
    }
}