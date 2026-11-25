package org.example.marlo_site_helpdesk.controller;

import jakarta.validation.Valid;
import org.example.marlo_site_helpdesk.dto.RegisterDto;
import org.example.marlo_site_helpdesk.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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

        AuthService.RegistrationResult result =
                authService.register(dto);

        switch (result) {
            case PASSWORD_MISMATCH:
                bindingResult.rejectValue(
                        "confirmPassword",
                        "password.mismatch",
                        "Пароли не совпадают"
                );
                break;

            case PASSWORD_TOO_LONG:
                bindingResult.rejectValue(
                        "password",
                        "password.length",
                        "Пароль слишком длинный"
                );
                break;

            case EMAIL_EXISTS:
                bindingResult.rejectValue(
                        "email",
                        "email.exists",
                        "Пользователь с такой почтой уже существует"
                );
                break;

            case SUCCESS:
                return "redirect:/login?registered";
        }

        return "register";
    }
}