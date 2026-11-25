package org.example.marlo_site_helpdesk.controller;

import org.example.marlo_site_helpdesk.config.CustomUserDetails;
import org.example.marlo_site_helpdesk.dto.TicketCreateDto;
import org.example.marlo_site_helpdesk.models.Category;
import org.example.marlo_site_helpdesk.models.Priority;
import org.example.marlo_site_helpdesk.models.Role;
import org.example.marlo_site_helpdesk.models.User;
import org.example.marlo_site_helpdesk.service.CommentService;
import org.example.marlo_site_helpdesk.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private CommentService commentService;

    private CustomUserDetails createPrincipal() {

        User user = new User();

        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPasswordHash("unused-password-hash");
        user.setRole(Role.USER);

        return new CustomUserDetails(user);
    }

    private void prepareCategories() {

        Category category = new Category();

        category.setId(1L);
        category.setName("Техническая проблема");

        when(ticketService.getCategories())
                .thenReturn(List.of(category));
    }

    private MockHttpServletRequestBuilder createRequest(
            String title,
            String description,
            String categoryId,
            String priority
    ) {

        MockHttpServletRequestBuilder request =
                post("/tickets/create")
                        .with(user(createPrincipal()))
                        .with(csrf());

        if (title != null) {
            request.param("title", title);
        }

        if (description != null) {
            request.param("description", description);
        }

        if (categoryId != null) {
            request.param("categoryId", categoryId);
        }

        if (priority != null) {
            request.param("priority", priority);
        }

        return request;
    }

    @Test
    @DisplayName("TC-01: Просмотр списка своих заявок")
    void getMyTicketsSuccess() throws Exception {

        when(ticketService.getMyTickets(1L))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/tickets/my")
                                .with(user(createPrincipal()))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/my"))
                .andExpect(model().attributeExists("tickets"));

        verify(ticketService).getMyTickets(1L);
    }

    @Test
    @DisplayName("TC-02: Открытие формы создания заявки")
    void getCreateFormSuccess() throws Exception {

        prepareCategories();

        mockMvc.perform(
                        get("/tickets/create")
                                .with(user(createPrincipal()))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/create"))
                .andExpect(model().attributeExists(
                        "ticketForm",
                        "categories",
                        "priorities"
                ));

        verify(ticketService).getCategories();
    }

    @Test
    @DisplayName("TC-03: Успешное создание заявки")
    void createTicketSuccess() throws Exception {

        when(ticketService.createTicket(
                any(TicketCreateDto.class),
                eq(1L)
        )).thenReturn(TicketService.CreateResult.SUCCESS);

        mockMvc.perform(
                        createRequest(
                                "Проблема с компьютером",
                                "Компьютер не включается",
                                "1",
                                "HIGH"
                        )
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/tickets/my"));

        ArgumentCaptor<TicketCreateDto> captor =
                ArgumentCaptor.forClass(TicketCreateDto.class);

        verify(ticketService)
                .createTicket(captor.capture(), eq(1L));

        TicketCreateDto dto = captor.getValue();

        assertEquals(
                "Проблема с компьютером",
                dto.getTitle()
        );

        assertEquals(
                "Компьютер не включается",
                dto.getDescription()
        );

        assertEquals(1L, dto.getCategoryId());
        assertEquals(Priority.HIGH, dto.getPriority());
    }

    @Test
    @DisplayName("TC-04: Пустой заголовок заявки")
    void createTicketEmptyTitle() throws Exception {

        prepareCategories();

        mockMvc.perform(
                        createRequest(
                                "",
                                "Описание проблемы",
                                "1",
                                "HIGH"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/create"))
                .andExpect(model().attributeHasFieldErrors(
                        "ticketForm",
                        "title"
                ));

        verify(ticketService, never())
                .createTicket(any(TicketCreateDto.class), anyLong());
    }

    @Test
    @DisplayName("TC-05: Пустое описание заявки")
    void createTicketEmptyDescription() throws Exception {

        prepareCategories();

        mockMvc.perform(
                        createRequest(
                                "Заголовок заявки",
                                "",
                                "1",
                                "HIGH"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/create"))
                .andExpect(model().attributeHasFieldErrors(
                        "ticketForm",
                        "description"
                ));

        verify(ticketService, never())
                .createTicket(any(TicketCreateDto.class), anyLong());
    }

    @Test
    @DisplayName("TC-06: Категория не выбрана")
    void createTicketWithoutCategory() throws Exception {

        prepareCategories();

        mockMvc.perform(
                        createRequest(
                                "Заголовок заявки",
                                "Описание проблемы",
                                null,
                                "HIGH"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/create"))
                .andExpect(model().attributeHasFieldErrors(
                        "ticketForm",
                        "categoryId"
                ));

        verify(ticketService, never())
                .createTicket(any(TicketCreateDto.class), anyLong());
    }

    @Test
    @DisplayName("TC-07: Приоритет не выбран")
    void createTicketWithoutPriority() throws Exception {

        prepareCategories();

        mockMvc.perform(
                        createRequest(
                                "Заголовок заявки",
                                "Описание проблемы",
                                "1",
                                null
                        )
                )
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/create"))
                .andExpect(model().attributeHasFieldErrors(
                        "ticketForm",
                        "priority"
                ));

        verify(ticketService, never())
                .createTicket(any(TicketCreateDto.class), anyLong());
    }

    @Test
    @DisplayName("TC-08: Заголовок длиннее 200 символов")
    void createTicketTitleTooLong() throws Exception {

        prepareCategories();

        mockMvc.perform(
                        createRequest(
                                "a".repeat(201),
                                "Описание проблемы",
                                "1",
                                "HIGH"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/create"))
                .andExpect(model().attributeHasFieldErrors(
                        "ticketForm",
                        "title"
                ));

        verify(ticketService, never())
                .createTicket(any(TicketCreateDto.class), anyLong());
    }

    @Test
    @DisplayName("TC-09: Несуществующая категория")
    void createTicketCategoryNotFound() throws Exception {

        prepareCategories();

        when(ticketService.createTicket(
                any(TicketCreateDto.class),
                eq(1L)
        )).thenReturn(
                TicketService.CreateResult.CATEGORY_NOT_FOUND
        );

        mockMvc.perform(
                        createRequest(
                                "Заголовок заявки",
                                "Описание проблемы",
                                "999",
                                "HIGH"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/create"))
                .andExpect(model().attributeHasFieldErrors(
                        "ticketForm",
                        "categoryId"
                ));

        verify(ticketService)
                .createTicket(any(TicketCreateDto.class), eq(1L));
    }

    @Test
    @DisplayName("TC-10: Просмотр чужой заявки запрещен")
    void getForeignTicketDenied() throws Exception {

        when(ticketService.getOwnTicket(10L, 1L))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND
                        )
                );

        mockMvc.perform(
                        get("/tickets/10")
                                .with(user(createPrincipal()))
                )
                .andExpect(status().isNotFound());

        verify(ticketService).getOwnTicket(10L, 1L);

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("TC-11: Несуществующая заявка")
    void getTicketNotFound() throws Exception {

        when(ticketService.getOwnTicket(999L, 1L))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND
                        )
                );

        mockMvc.perform(
                        get("/tickets/999")
                                .with(user(createPrincipal()))
                )
                .andExpect(status().isNotFound());

        verify(ticketService).getOwnTicket(999L, 1L);

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("TC-12: Описание длиннее 5000 символов")
    void createTicketDescriptionTooLong() throws Exception {

        prepareCategories();

        mockMvc.perform(
                        createRequest(
                                "Заголовок заявки",
                                "a".repeat(5001),
                                "1",
                                "HIGH"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/create"))
                .andExpect(model().attributeHasFieldErrors(
                        "ticketForm",
                        "description"
                ));

        verify(ticketService, never())
                .createTicket(any(TicketCreateDto.class), anyLong());
    }
}