package org.example.marlo_site_helpdesk.controller;

import org.example.marlo_site_helpdesk.config.CustomUserDetails;
import org.example.marlo_site_helpdesk.models.Role;
import org.example.marlo_site_helpdesk.models.User;
import org.example.marlo_site_helpdesk.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.context.ActiveProfiles;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    private CustomUserDetails createPrincipal(
            Long id,
            Role role
    ) {

        User user = new User();

        user.setId(id);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPasswordHash("unused-password-hash");
        user.setRole(role);

        return new CustomUserDetails(user);
    }

    @Test
    @DisplayName("CC-01: Отправка комментария пользователем")
    void addUserCommentSuccess() throws Exception {

        mockMvc.perform(
                        post("/tickets/10/comments")
                                .with(user(createPrincipal(1L, Role.USER)))
                                .with(csrf())
                                .param("text", "Новый комментарий")
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/tickets/10"));

        verify(commentService).addComment(
                10L,
                1L,
                "Новый комментарий"
        );
    }

    @Test
    @DisplayName("CC-02: Отправка ответа администратором")
    void addAdminCommentSuccess() throws Exception {

        mockMvc.perform(
                        post("/admin/tickets/10/comments")
                                .with(user(createPrincipal(2L, Role.ADMIN)))
                                .with(csrf())
                                .param("text", "Заявка принята в работу")
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin/tickets/10"));

        verify(commentService).addComment(
                10L,
                2L,
                "Заявка принята в работу"
        );
    }

    @Test
    @DisplayName("CC-03: Пустой комментарий")
    void addEmptyComment() throws Exception {

        mockMvc.perform(
                        post("/tickets/10/comments")
                                .with(user(createPrincipal(1L, Role.USER)))
                                .with(csrf())
                                .param("text", "")
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/tickets/10"))
                .andExpect(flash().attributeExists("commentError"));

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("CC-04: Комментарий из пробелов")
    void addBlankComment() throws Exception {

        mockMvc.perform(
                        post("/tickets/10/comments")
                                .with(user(createPrincipal(1L, Role.USER)))
                                .with(csrf())
                                .param("text", "   ")
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/tickets/10"))
                .andExpect(flash().attributeExists("commentError"));

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("CC-05: Отсутствие текста комментария")
    void addCommentWithoutText() throws Exception {

        mockMvc.perform(
                        post("/tickets/10/comments")
                                .with(user(createPrincipal(1L, Role.USER)))
                                .with(csrf())
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/tickets/10"))
                .andExpect(flash().attributeExists("commentError"));

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("CC-06: Комментарий длиннее 3000 символов")
    void addTooLongComment() throws Exception {

        mockMvc.perform(
                        post("/tickets/10/comments")
                                .with(user(createPrincipal(1L, Role.USER)))
                                .with(csrf())
                                .param("text", "a".repeat(3001))
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/tickets/10"))
                .andExpect(flash().attributeExists("commentError"));

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("CC-07: Комментарий длиной 3000 символов")
    void addMaximumLengthComment() throws Exception {

        String text = "a".repeat(3000);

        mockMvc.perform(
                        post("/tickets/10/comments")
                                .with(user(createPrincipal(1L, Role.USER)))
                                .with(csrf())
                                .param("text", text)
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/tickets/10"))
                .andExpect(flash().attributeCount(0));

        verify(commentService).addComment(
                10L,
                1L,
                text
        );
    }

    @Test
    @DisplayName("CC-08: Комментирование чужой заявки")
    void addForeignCommentDenied() throws Exception {

        doThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND)
        ).when(commentService).addComment(
                10L,
                3L,
                "Чужой комментарий"
        );

        mockMvc.perform(
                        post("/tickets/10/comments")
                                .with(user(createPrincipal(3L, Role.USER)))
                                .with(csrf())
                                .param("text", "Чужой комментарий")
                )
                .andExpect(status().isNotFound());

        verify(commentService).addComment(
                10L,
                3L,
                "Чужой комментарий"
        );
    }

    @Test
    @DisplayName("CC-09: Комментирование закрытой заявки")
    void addCommentToClosedTicket() throws Exception {

        doThrow(
                new ResponseStatusException(HttpStatus.BAD_REQUEST)
        ).when(commentService).addComment(
                10L,
                1L,
                "Новый комментарий"
        );

        mockMvc.perform(
                        post("/tickets/10/comments")
                                .with(user(createPrincipal(1L, Role.USER)))
                                .with(csrf())
                                .param("text", "Новый комментарий")
                )
                .andExpect(status().isBadRequest());

        verify(commentService).addComment(
                10L,
                1L,
                "Новый комментарий"
        );
    }

    @Test
    @DisplayName("CC-10: Отправка комментария без CSRF")
    void addCommentWithoutCsrf() throws Exception {

        mockMvc.perform(
                        post("/tickets/10/comments")
                                .with(user(createPrincipal(1L, Role.USER)))
                                .param("text", "Новый комментарий")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("CC-11: Пользователь использует маршрут администратора")
    void userCannotAddAdminComment() throws Exception {

        mockMvc.perform(
                        post("/admin/tickets/10/comments")
                                .with(user(createPrincipal(1L, Role.USER)))
                                .with(csrf())
                                .param("text", "Ответ администратора")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("CC-12: Отправка комментария без авторизации")
    void anonymousCannotAddComment() throws Exception {

        mockMvc.perform(
                        post("/tickets/10/comments")
                                .with(csrf())
                                .param("text", "Новый комментарий")
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(commentService);
    }
}