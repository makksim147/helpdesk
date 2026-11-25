package org.example.marlo_site_helpdesk.service;

import org.example.marlo_site_helpdesk.models.*;
import org.example.marlo_site_helpdesk.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository) {

        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Comment> getComments(Long ticketId, String email) {

        User user = getUser(email);

        Ticket ticket = getAvailableTicket(ticketId, user);

        return commentRepository
                .findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId());
    }

    @Transactional
    public void addComment(Long ticketId, String email, String text) {

        User author = getUser(email);

        Ticket ticket = getAvailableTicket(ticketId, author);

        if (ticket.getStatus() == Status.CLOSED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Нельзя комментировать закрытую заявку"
            );
        }

        if (text == null || text.isBlank() || text.length() > 3000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Некорректный текст комментария"
            );
        }

        Comment comment = new Comment();

        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setText(text.trim());

        commentRepository.save(comment);
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED
                ));
    }

    private Ticket getAvailableTicket(Long ticketId, User user) {

        if (user.getRole() == Role.ADMIN) {

            return ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Заявка не найдена"
                    ));
        }

        return ticketRepository
                .findByIdAndAuthorEmail(ticketId, user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Заявка не найдена"
                ));
    }
}