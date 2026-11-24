package org.example.marlo_site_helpdesk.Controller;

import org.example.marlo_site_helpdesk.Models.*;
import org.example.marlo_site_helpdesk.Repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TicketController(TicketRepository ticketRepository,
                            CategoryRepository categoryRepository,
                            UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    public String myTickets(Authentication auth, Model model) {
        List<Ticket> tickets = ticketRepository.findByAuthorEmailOrderByCreatedAtDesc(auth.getName());
        model.addAttribute("tickets", tickets);
        model.addAttribute("username", auth.getName());
        return "tickets/my";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("priorities", Priority.values());
        return "tickets/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String title,
                         @RequestParam String description,
                         @RequestParam Long categoryId,
                         @RequestParam Priority priority,
                         Authentication auth) {
        User author = userRepository.findByEmail(auth.getName()).orElseThrow();
        Category category = categoryRepository.findById(categoryId).orElseThrow();

        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setAuthor(author);
        ticket.setStatus(Status.NEW);

        ticketRepository.save(ticket);
        return "redirect:/tickets/my";
    }

    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Long id, Model model, Authentication auth) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        model.addAttribute("ticket", ticket);
        model.addAttribute("username", auth.getName());
        return "tickets/view";
    }
}