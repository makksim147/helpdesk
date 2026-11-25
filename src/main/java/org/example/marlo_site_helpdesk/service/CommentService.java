package org.example.marlo_site_helpdesk.service;

import org.example.marlo_site_helpdesk.models.*;
import org.example.marlo_site_helpdesk.repository.CommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketService ticketService;
    private final UserService userService;

    public CommentService(
            CommentRepository commentRepository,
            TicketService ticketService,
            UserService userService) {

        this.commentRepository = commentRepository;
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Comment> getComments(Long ticketId, Long userId) {

        User user = userService.getUser(userId);

        Ticket ticket =
                ticketService.getAvailableTicket(ticketId, user);

        return commentRepository
                .findByTicketIdOrderByCreatedAtAscIdAsc(
                        ticket.getId()
                );
    }

    @Transactional
    public void addComment(
            Long ticketId,
            Long userId,
            String text) {

        User author = userService.getUser(userId);

        Ticket ticket =
                ticketService.getAvailableTicket(ticketId, author);

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
}