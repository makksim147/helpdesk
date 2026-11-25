package org.example.marlo_site_helpdesk.controller;

import jakarta.validation.Valid;
import org.example.marlo_site_helpdesk.config.CustomUserDetails;
import org.example.marlo_site_helpdesk.dto.CommentDto;
import org.example.marlo_site_helpdesk.service.CommentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/tickets/{id}/comments")
    public String addUserComment(
            @PathVariable Long id,
            @Valid @ModelAttribute CommentDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails principal,
            RedirectAttributes redirectAttributes) {

        return addComment(
                id,
                dto,
                bindingResult,
                principal,
                redirectAttributes,
                "/tickets/" + id
        );
    }

    @PostMapping("/admin/tickets/{id}/comments")
    public String addAdminComment(
            @PathVariable Long id,
            @Valid @ModelAttribute CommentDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails principal,
            RedirectAttributes redirectAttributes) {

        return addComment(
                id,
                dto,
                bindingResult,
                principal,
                redirectAttributes,
                "/admin/tickets/" + id
        );
    }

    private String addComment(
            Long id,
            CommentDto dto,
            BindingResult bindingResult,
            CustomUserDetails principal,
            RedirectAttributes redirectAttributes,
            String redirectUrl) {

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute(
                    "commentError",
                    bindingResult.getFieldError("text")
                            .getDefaultMessage()
            );

            return "redirect:" + redirectUrl;
        }

        commentService.addComment(
                id,
                principal.getId(),
                dto.getText()
        );

        return "redirect:" + redirectUrl;
    }
}