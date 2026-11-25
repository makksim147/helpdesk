package org.example.marlo_site_helpdesk.controller;

import jakarta.validation.Valid;
import org.example.marlo_site_helpdesk.models.*;
import org.example.marlo_site_helpdesk.repository.TicketRepository;
import org.example.marlo_site_helpdesk.dto.TicketUpdateDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final TicketRepository ticketRepository;

    public AdminController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public String adminPage() {
        return "redirect:/admin/tickets";
    }

    @GetMapping("/tickets")
    public String tickets(Model model) {

        model.addAttribute(
                "tickets",
                ticketRepository.findAllByOrderByCreatedAtDesc()
        );

        return "admin/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String viewTicket(
            @PathVariable Long id,
            Model model) {

        Ticket ticket = getTicket(id);

        TicketUpdateDto dto = new TicketUpdateDto();

        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());

        model.addAttribute("ticketForm", dto);

        fillModel(model, ticket);

        return "admin/view";
    }

    @PostMapping("/tickets/{id}/update")
    public String updateTicket(
            @PathVariable Long id,
            @Valid @ModelAttribute("ticketForm") TicketUpdateDto dto,
            BindingResult bindingResult,
            Model model) {

        Ticket ticket = getTicket(id);

        if (bindingResult.hasErrors()) {

            fillModel(model, ticket);

            return "admin/view";
        }

        ticket.setStatus(dto.getStatus());
        ticket.setPriority(dto.getPriority());

        ticketRepository.save(ticket);

        return "redirect:/admin/tickets/" + id + "?success";
    }

    @PostMapping("/tickets/{id}/close")
    public String closeTicket(@PathVariable Long id) {

        Ticket ticket = getTicket(id);

        if (ticket.getStatus() != Status.DONE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Закрыть можно только выполненную заявку"
            );
        }

        ticket.setStatus(Status.CLOSED);

        ticketRepository.save(ticket);

        return "redirect:/admin/tickets/" + id + "?success";
    }

    private Ticket getTicket(Long id) {

        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Заявка не найдена"
                ));
    }

    private void fillModel(Model model, Ticket ticket) {

        model.addAttribute("ticket", ticket);
        model.addAttribute("statuses", Status.values());
        model.addAttribute("priorities", Priority.values());
    }
}