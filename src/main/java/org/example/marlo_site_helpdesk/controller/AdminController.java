package org.example.marlo_site_helpdesk.controller;

import jakarta.validation.Valid;
import org.example.marlo_site_helpdesk.models.*;
import org.example.marlo_site_helpdesk.dto.TicketUpdateDto;
import org.example.marlo_site_helpdesk.dto.TicketFilterDto;
import org.example.marlo_site_helpdesk.service.CommentService;
import org.example.marlo_site_helpdesk.service.TicketService;
import org.example.marlo_site_helpdesk.service.StatisticsService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CommentService commentService;
    private final TicketService ticketService;
    private final StatisticsService statisticsService;

    public AdminController(
            CommentService commentService,
            TicketService ticketService,
            StatisticsService statisticsService) {

        this.commentService = commentService;
        this.ticketService = ticketService;
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public String adminPage() {
        return "redirect:/admin/tickets";
    }

    @GetMapping("/tickets")
    public String tickets(
            @ModelAttribute("filter") TicketFilterDto filter,
            Model model) {

        model.addAttribute(
                "tickets",
                ticketService.findTickets(filter)
        );

        model.addAttribute(
                "categories",
                ticketService.getCategories()
        );

        model.addAttribute(
                "statistics",
                statisticsService.getStatistics()
        );

        model.addAttribute("statuses", Status.values());
        model.addAttribute("priorities", Priority.values());

        return "admin/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String viewTicket(
            @PathVariable Long id,
            Model model,
            Authentication auth) {

        Ticket ticket = ticketService.getTicket(id);

        TicketUpdateDto dto = new TicketUpdateDto();

        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());

        model.addAttribute("ticketForm", dto);

        fillModel(model, ticket, auth);

        return "admin/view";
    }

    @PostMapping("/tickets/{id}/update")
    public String updateTicket(
            @PathVariable Long id,
            @Valid @ModelAttribute("ticketForm") TicketUpdateDto dto,
            BindingResult bindingResult,
            Model model,
            Authentication auth) {

        if (bindingResult.hasErrors()) {

            Ticket ticket = ticketService.getTicket(id);

            fillModel(model, ticket, auth);

            return "admin/view";
        }

        ticketService.updateTicket(id, dto);

        return "redirect:/admin/tickets/" + id + "?success";
    }

    @PostMapping("/tickets/{id}/close")
    public String closeTicket(@PathVariable Long id) {

        ticketService.closeTicket(id);

        return "redirect:/admin/tickets/" + id + "?success";
    }

    private void fillModel(
            Model model,
            Ticket ticket,
            Authentication auth) {

        model.addAttribute("ticket", ticket);
        model.addAttribute("statuses", Status.values());
        model.addAttribute("priorities", Priority.values());

        model.addAttribute(
                "comments",
                commentService.getComments(
                        ticket.getId(),
                        auth.getName()
                )
        );
    }
}