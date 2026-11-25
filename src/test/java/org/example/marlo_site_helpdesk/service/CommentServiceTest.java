package org.example.marlo_site_helpdesk.service;

import org.example.marlo_site_helpdesk.models.*;
import org.example.marlo_site_helpdesk.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketService ticketService;

    @Mock
    private UserService userService;

    private CommentService commentService;

    private User user;
    private User admin;
    private Ticket ticket;
    private Comment comment;

    @BeforeEach
    void setUp() {

        commentService = new CommentService(
                commentRepository,
                ticketService,
                userService
        );

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setRole(Role.USER);

        admin = new User();
        admin.setId(2L);
        admin.setName("Admin");
        admin.setRole(Role.ADMIN);

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setAuthor(user);
        ticket.setStatus(Status.NEW);

        comment = new Comment();
        comment.setId(1L);
        comment.setTicket(ticket);
        comment.setAuthor(user);
        comment.setText("Тестовый комментарий");
    }

    private void allowOwnerAccess() {

        when(userService.getUser(1L))
                .thenReturn(user);

        when(ticketService.getAvailableTicket(10L, user))
                .thenReturn(ticket);
    }

    @Test
    @DisplayName("COM-01: Получение комментариев")
    void getCommentsSuccess() {

        allowOwnerAccess();

        when(commentRepository
                .findByTicketIdOrderByCreatedAtAscIdAsc(10L))
                .thenReturn(List.of(comment));

        List<Comment> result =
                commentService.getComments(10L, 1L);

        assertEquals(1, result.size());
        assertEquals(comment, result.get(0));

        verify(commentRepository)
                .findByTicketIdOrderByCreatedAtAscIdAsc(10L);
    }

    @Test
    @DisplayName("COM-02: Заявка без комментариев")
    void getEmptyComments() {

        allowOwnerAccess();

        when(commentRepository
                .findByTicketIdOrderByCreatedAtAscIdAsc(10L))
                .thenReturn(List.of());

        List<Comment> result =
                commentService.getComments(10L, 1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("COM-03: Просмотр чужих комментариев")
    void getForeignCommentsDenied() {

        User anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setRole(Role.USER);

        when(userService.getUser(3L))
                .thenReturn(anotherUser);

        when(ticketService.getAvailableTicket(10L, anotherUser))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND
                        )
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.getComments(10L, 3L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("COM-04: Добавление комментария владельцем")
    void addCommentSuccess() {

        allowOwnerAccess();

        commentService.addComment(
                10L,
                1L,
                "  Новый комментарий  "
        );

        ArgumentCaptor<Comment> captor =
                ArgumentCaptor.forClass(Comment.class);

        verify(commentRepository).save(captor.capture());

        Comment savedComment = captor.getValue();

        assertEquals(ticket, savedComment.getTicket());
        assertEquals(user, savedComment.getAuthor());

        assertEquals(
                "Новый комментарий",
                savedComment.getText()
        );
    }

    @Test
    @DisplayName("COM-05: Ответ администратора")
    void addAdminCommentSuccess() {

        when(userService.getUser(2L))
                .thenReturn(admin);

        when(ticketService.getAvailableTicket(10L, admin))
                .thenReturn(ticket);

        commentService.addComment(
                10L,
                2L,
                "Ваш запрос принят в работу"
        );

        ArgumentCaptor<Comment> captor =
                ArgumentCaptor.forClass(Comment.class);

        verify(commentRepository).save(captor.capture());

        Comment savedComment = captor.getValue();

        assertEquals(admin, savedComment.getAuthor());
        assertEquals(ticket, savedComment.getTicket());

        assertEquals(
                Role.ADMIN,
                savedComment.getAuthor().getRole()
        );

        assertEquals(
                "Ваш запрос принят в работу",
                savedComment.getText()
        );
    }

    @Test
    @DisplayName("COM-06: Комментирование чужой заявки")
    void addForeignCommentDenied() {

        User anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setRole(Role.USER);

        when(userService.getUser(3L))
                .thenReturn(anotherUser);

        when(ticketService.getAvailableTicket(10L, anotherUser))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND
                        )
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(
                        10L,
                        3L,
                        "Чужой комментарий"
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("COM-07: Комментирование закрытой заявки")
    void addCommentToClosedTicket() {

        ticket.setStatus(Status.CLOSED);

        allowOwnerAccess();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(
                        10L,
                        1L,
                        "Новый комментарий"
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("COM-08: Пустой комментарий")
    void addEmptyComment() {

        allowOwnerAccess();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(10L, 1L, "")
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("COM-09: Комментарий из пробелов")
    void addBlankComment() {

        allowOwnerAccess();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(10L, 1L, "   ")
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("COM-10: Комментарий со значением null")
    void addNullComment() {

        allowOwnerAccess();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(10L, 1L, null)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("COM-11: Комментарий длиной 3001 символ")
    void addTooLongComment() {

        allowOwnerAccess();

        String text = "a".repeat(3001);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(10L, 1L, text)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("COM-12: Комментарий длиной 3000 символов")
    void addMaximumLengthComment() {

        allowOwnerAccess();

        String text = "a".repeat(3000);

        commentService.addComment(10L, 1L, text);

        ArgumentCaptor<Comment> captor =
                ArgumentCaptor.forClass(Comment.class);

        verify(commentRepository).save(captor.capture());

        Comment savedComment = captor.getValue();

        assertEquals(3000, savedComment.getText().length());
        assertEquals(text, savedComment.getText());
        assertEquals(user, savedComment.getAuthor());
        assertEquals(ticket, savedComment.getTicket());
    }
}