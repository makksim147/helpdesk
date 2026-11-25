package org.example.marlo_site_helpdesk.controller;

import jakarta.validation.Valid;
import org.example.marlo_site_helpdesk.models.*;
import org.example.marlo_site_helpdesk.repository.*;
import org.example.marlo_site_helpdesk.dto.TicketCreateDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.example.marlo_site_helpdesk.service.CommentService;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CommentService commentService;

    public TicketController(
            TicketRepository ticketRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            CommentService commentService) {

        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.commentService = commentService;
    }

    @GetMapping("/my")
    public String myTickets(Authentication auth, Model model) {

        List<Ticket> tickets =
                ticketRepository.findByAuthorEmailOrderByCreatedAtDesc(
                        auth.getName()
                );

        model.addAttribute("tickets", tickets);
        model.addAttribute("username", auth.getName());

        return "tickets/my";
    }

    @GetMapping("/create")
    public String createForm(Model model) {

        model.addAttribute("ticketForm", new TicketCreateDto());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("priorities", Priority.values());

        return "tickets/create";
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("ticketForm") TicketCreateDto dto,
            BindingResult bindingResult,
            Authentication auth,
            Model model) {

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("priorities", Priority.values());

        if (bindingResult.hasErrors()) {
            return "tickets/create";
        }

        Optional<Category> category =
                categoryRepository.findById(dto.getCategoryId());

        if (category.isEmpty()) {

            bindingResult.rejectValue(
                    "categoryId",
                    "category.notFound",
                    "Категория не найдена"
            );

            return "tickets/create";
        }

        User author = userRepository.findByEmail(auth.getName())
                .orElseThrow();

        Ticket ticket = new Ticket();

        ticket.setTitle(dto.getTitle().trim());
        ticket.setDescription(dto.getDescription().trim());
        ticket.setCategory(category.get());
        ticket.setPriority(dto.getPriority());
        ticket.setAuthor(author);
        ticket.setStatus(Status.NEW);

        ticketRepository.save(ticket);

        return "redirect:/tickets/my";
    }

    @GetMapping("/{id}")
    public String viewTicket(
            @PathVariable Long id,
            Model model,
            Authentication auth) {

        Ticket ticket = ticketRepository
                .findByIdAndAuthorEmail(id, auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Заявка не найдена"
                ));

        model.addAttribute("ticket", ticket);
        model.addAttribute("username", auth.getName());

        model.addAttribute(
                "comments",
                commentService.getComments(id, auth.getName())
        );

        return "tickets/view";
    }
}