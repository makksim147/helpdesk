package org.example.marlo_site_helpdesk.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.marlo_site_helpdesk.config.CustomUserDetails;
import org.example.marlo_site_helpdesk.dto.ProfileDto;
import org.example.marlo_site_helpdesk.models.User;
import org.example.marlo_site_helpdesk.service.ProfileService;
import org.example.marlo_site_helpdesk.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    public ProfileController(
            ProfileService profileService,
            UserService userService) {

        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping
    public String profile(
            @AuthenticationPrincipal CustomUserDetails principal,
            Model model) {

        User user = userService.getUser(principal.getId());

        model.addAttribute(
                "profile",
                profileService.getProfile(user)
        );

        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping
    public String updateProfile(
            @Valid @ModelAttribute("profile") ProfileDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails principal,
            Authentication auth,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {

        Long userId = principal.getId();

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "user",
                    userService.getUser(userId)
            );

            return "profile";
        }

        ProfileService.UpdateResult result =
                profileService.updateProfile(userId, dto);

        if (result == ProfileService.UpdateResult.EMAIL_EXISTS) {

            bindingResult.rejectValue(
                    "email",
                    "email.exists",
                    "Эта почта уже используется"
            );

            model.addAttribute(
                    "user",
                    userService.getUser(userId)
            );

            return "profile";
        }

        if (result == ProfileService.UpdateResult.EMAIL_CHANGED) {

            new SecurityContextLogoutHandler()
                    .logout(request, response, auth);

            return "redirect:/login?emailChanged";
        }

        return "redirect:/profile?success";
    }
}