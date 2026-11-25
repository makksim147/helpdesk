package org.example.marlo_site_helpdesk.config;

import org.example.marlo_site_helpdesk.models.Role;
import org.example.marlo_site_helpdesk.models.User;
import org.example.marlo_site_helpdesk.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    private CustomUserDetails createPrincipal(Role role) {

        User user = new User();

        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPasswordHash("unused-password-hash");
        user.setRole(role);

        return new CustomUserDetails(user);
    }

    private void prepareLogin(Role role) {

        User user = new User();

        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );
        user.setRole(role);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("SEC-01: Главная страница доступна всем")
    void homePagePublic() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SEC-02: Страница входа доступна всем")
    void loginPagePublic() throws Exception {

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SEC-03: Страница регистрации доступна всем")
    void registerPagePublic() throws Exception {

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SEC-04: Заявки требуют авторизации")
    void ticketsRequireAuthentication() throws Exception {

        mockMvc.perform(get("/tickets/my"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("SEC-05: Профиль требует авторизации")
    void profileRequiresAuthentication() throws Exception {

        mockMvc.perform(get("/profile"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("SEC-06: Админ-панель требует авторизации")
    void adminRequiresAuthentication() throws Exception {

        mockMvc.perform(get("/admin/tickets"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("SEC-07: Пользователю запрещен доступ к админ-панели")
    void userCannotAccessAdminPanel() throws Exception {

        mockMvc.perform(
                        get("/admin/tickets")
                                .with(user(createPrincipal(Role.USER)))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-08: Пользователь не может изменить статус заявки")
    void userCannotUpdateTicket() throws Exception {

        mockMvc.perform(
                        post("/admin/tickets/10/update")
                                .with(user(createPrincipal(Role.USER)))
                                .with(csrf())
                                .param("status", "DONE")
                                .param("priority", "HIGH")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-09: Пользователь не может отправить ответ администратора")
    void userCannotAddAdminComment() throws Exception {

        mockMvc.perform(
                        post("/admin/tickets/10/comments")
                                .with(user(createPrincipal(Role.USER)))
                                .with(csrf())
                                .param("text", "Test comment")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-10: Пользователь не может закрыть заявку")
    void userCannotCloseTicket() throws Exception {

        mockMvc.perform(
                        post("/admin/tickets/10/close")
                                .with(user(createPrincipal(Role.USER)))
                                .with(csrf())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-11: Создание заявки без CSRF запрещено")
    void createTicketWithoutCsrf() throws Exception {

        mockMvc.perform(
                        post("/tickets/create")
                                .with(user(createPrincipal(Role.USER)))
                                .param("title", "Test ticket")
                                .param("description", "Test description")
                                .param("categoryId", "1")
                                .param("priority", "HIGH")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-12: Администратору также необходим CSRF")
    void adminUpdateWithoutCsrf() throws Exception {

        mockMvc.perform(
                        post("/admin/tickets/10/update")
                                .with(user(createPrincipal(Role.ADMIN)))
                                .param("status", "DONE")
                                .param("priority", "HIGH")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-13: Выход без CSRF запрещен")
    void logoutWithoutCsrf() throws Exception {

        mockMvc.perform(
                        post("/logout")
                                .with(user(createPrincipal(Role.USER)))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-14: Успешный выход из аккаунта")
    void logoutSuccess() throws Exception {

        mockMvc.perform(
                        post("/logout")
                                .with(user(createPrincipal(Role.USER)))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("SEC-15: Вход с неверным паролем")
    void loginWrongPassword() throws Exception {

        prepareLogin(Role.USER);

        mockMvc.perform(
                        formLogin("/login")
                                .user("test@example.com")
                                .password("wrongPassword")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("SEC-16: Успешный вход пользователя")
    void loginUserSuccess() throws Exception {

        prepareLogin(Role.USER);

        mockMvc.perform(
                        formLogin("/login")
                                .user("test@example.com")
                                .password("password123")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets/my"))
                .andExpect(authenticated().withRoles("USER"));
    }

    @Test
    @DisplayName("SEC-17: Успешный вход администратора")
    void loginAdminSuccess() throws Exception {

        prepareLogin(Role.ADMIN);

        mockMvc.perform(
                        formLogin("/login")
                                .user("test@example.com")
                                .password("password123")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets"))
                .andExpect(authenticated().withRoles("ADMIN"));
    }
}