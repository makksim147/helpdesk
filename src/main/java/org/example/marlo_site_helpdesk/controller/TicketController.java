package org.example.marlo_site_helpdesk.controller;

import jakarta.validation.Valid;
import org.example.marlo_site_helpdesk.models.Priority;
import org.example.marlo_site_helpdesk.models.Ticket;
import org.example.marlo_site_helpdesk.dto.TicketCreateDto;
import org.example.marlo_site_helpdesk.service.CommentService;
import org.example.marlo_site_helpdesk.service.TicketService;
import org.example.marlo_site_helpdesk.config.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService;

    public TicketController(
            TicketService ticketService,
            CommentService commentService) {

        this.ticketService = ticketService;
        this.commentService = commentService;
    }

    @GetMapping("/my")
    public String myTickets(
            @AuthenticationPrincipal CustomUserDetails principal,
            Model model) {

        model.addAttribute(
                "tickets",
                ticketService.getMyTickets(principal.getId())
        );

        return "tickets/my";
    }

    @GetMapping("/create")
    public String createForm(Model model) {

        model.addAttribute("ticketForm", new TicketCreateDto());

        fillFormModel(model);

        return "tickets/create";
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("ticketForm") TicketCreateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails principal,
            Model model) {

        if (bindingResult.hasErrors()) {

            fillFormModel(model);

            return "tickets/create";
        }

        TicketService.CreateResult result =
                ticketService.createTicket(dto, principal.getId());

        if (result == TicketService.CreateResult.CATEGORY_NOT_FOUND) {

            bindingResult.rejectValue(
                    "categoryId",
                    "category.notFound",
                    "Категория не найдена"
            );

            fillFormModel(model);

            return "tickets/create";
        }

        return "redirect:/tickets/my";
    }

    @GetMapping("/{id}")
    public String viewTicket(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Ticket ticket = ticketService.getOwnTicket(
                id,
                principal.getId()
        );

        model.addAttribute("ticket", ticket);

        model.addAttribute(
                "comments",
                commentService.getComments(id, principal.getId())
        );

        return "tickets/view";
    }

    private void fillFormModel(Model model) {

        model.addAttribute(
                "categories",
                ticketService.getCategories()
        );

        model.addAttribute("priorities", Priority.values());
    }
}