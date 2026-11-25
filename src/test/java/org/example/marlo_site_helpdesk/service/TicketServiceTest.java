package org.example.marlo_site_helpdesk.service;

import org.example.marlo_site_helpdesk.dto.TicketCreateDto;
import org.example.marlo_site_helpdesk.dto.TicketFilterDto;
import org.example.marlo_site_helpdesk.dto.TicketUpdateDto;
import org.example.marlo_site_helpdesk.models.*;
import org.example.marlo_site_helpdesk.repository.CategoryRepository;
import org.example.marlo_site_helpdesk.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    private TicketService ticketService;

    private User user;
    private User admin;
    private Category category;
    private Ticket ticket;

    @BeforeEach
    void setUp() {

        ticketService = new TicketService(
                ticketRepository,
                categoryRepository,
                userService
        );

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);

        admin = new User();
        admin.setId(2L);
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);

        category = new Category();
        category.setId(1L);
        category.setName("Техническая проблема");

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setTitle("Проблема с компьютером");
        ticket.setDescription("Компьютер не включается");
        ticket.setCategory(category);
        ticket.setAuthor(user);
        ticket.setPriority(Priority.MEDIUM);
        ticket.setStatus(Status.NEW);
    }

    private TicketCreateDto createDto() {

        TicketCreateDto dto = new TicketCreateDto();

        dto.setTitle("  Проблема с компьютером  ");
        dto.setDescription("  Компьютер не включается  ");
        dto.setCategoryId(1L);
        dto.setPriority(Priority.MEDIUM);

        return dto;
    }

    @Test
    @DisplayName("TICK-01: Успешное создание заявки")
    void createTicketSuccess() {

        TicketCreateDto dto = createDto();

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(userService.getUser(1L))
                .thenReturn(user);

        TicketService.CreateResult result =
                ticketService.createTicket(dto, 1L);

        assertEquals(
                TicketService.CreateResult.SUCCESS,
                result
        );

        ArgumentCaptor<Ticket> captor =
                ArgumentCaptor.forClass(Ticket.class);

        verify(ticketRepository).save(captor.capture());

        Ticket savedTicket = captor.getValue();

        assertEquals(
                "Проблема с компьютером",
                savedTicket.getTitle()
        );

        assertEquals(
                "Компьютер не включается",
                savedTicket.getDescription()
        );

        assertEquals(category, savedTicket.getCategory());
        assertEquals(user, savedTicket.getAuthor());
        assertEquals(Priority.MEDIUM, savedTicket.getPriority());
        assertEquals(Status.NEW, savedTicket.getStatus());
    }

    @Test
    @DisplayName("TICK-02: Несуществующая категория")
    void createTicketCategoryNotFound() {

        TicketCreateDto dto = createDto();

        dto.setCategoryId(999L);

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        TicketService.CreateResult result =
                ticketService.createTicket(dto, 1L);

        assertEquals(
                TicketService.CreateResult.CATEGORY_NOT_FOUND,
                result
        );

        verifyNoInteractions(ticketRepository, userService);
    }

    @Test
    @DisplayName("TICK-03: Несуществующий автор заявки")
    void createTicketUserNotFound() {

        TicketCreateDto dto = createDto();

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(userService.getUser(999L))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED
                        )
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.createTicket(dto, 999L)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verifyNoInteractions(ticketRepository);
    }

    @Test
    @DisplayName("TICK-04: Получение списка своих заявок")
    void getMyTicketsSuccess() {

        when(userService.getUser(1L))
                .thenReturn(user);

        when(ticketRepository.findByAuthorIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(ticket));

        List<Ticket> result =
                ticketService.getMyTickets(1L);

        assertEquals(1, result.size());
        assertEquals(ticket, result.get(0));
        assertEquals(1L, result.get(0).getAuthor().getId());

        verify(userService).getUser(1L);

        verify(ticketRepository)
                .findByAuthorIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("TICK-05: Получение собственной заявки")
    void getOwnTicketSuccess() {

        when(ticketRepository.findByIdAndAuthorId(10L, 1L))
                .thenReturn(Optional.of(ticket));

        Ticket result =
                ticketService.getOwnTicket(10L, 1L);

        assertEquals(ticket, result);

        verify(ticketRepository)
                .findByIdAndAuthorId(10L, 1L);
    }

    @Test
    @DisplayName("TICK-06: Попытка получить чужую заявку")
    void getForeignTicketDenied() {

        when(ticketRepository.findByIdAndAuthorId(10L, 3L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.getOwnTicket(10L, 3L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(ticketRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("TICK-07: Получение заявки по ID")
    void getTicketSuccess() {

        when(ticketRepository.findById(10L))
                .thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicket(10L);

        assertEquals(ticket, result);

        verify(ticketRepository).findById(10L);
    }

    @Test
    @DisplayName("TICK-08: Несуществующая заявка")
    void getTicketNotFound() {

        when(ticketRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.getTicket(999L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    @DisplayName("TICK-09: Изменение статуса и приоритета")
    void updateTicketSuccess() {

        TicketUpdateDto dto = new TicketUpdateDto();

        dto.setStatus(Status.IN_PROGRESS);
        dto.setPriority(Priority.HIGH);

        when(ticketRepository.findById(10L))
                .thenReturn(Optional.of(ticket));

        ticketService.updateTicket(10L, dto);

        assertEquals(Status.IN_PROGRESS, ticket.getStatus());
        assertEquals(Priority.HIGH, ticket.getPriority());

        assertEquals(user, ticket.getAuthor());
        assertEquals(category, ticket.getCategory());

        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("TICK-10: Закрытие выполненной заявки")
    void closeDoneTicketSuccess() {

        ticket.setStatus(Status.DONE);

        when(ticketRepository.findById(10L))
                .thenReturn(Optional.of(ticket));

        ticketService.closeTicket(10L);

        assertEquals(Status.CLOSED, ticket.getStatus());

        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("TICK-11: Закрытие невыполненной заявки")
    void closeUnfinishedTicketDenied() {

        ticket.setStatus(Status.IN_PROGRESS);

        when(ticketRepository.findById(10L))
                .thenReturn(Optional.of(ticket));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.closeTicket(10L)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(Status.IN_PROGRESS, ticket.getStatus());

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TICK-12: Доступ администратора к заявке")
    void getAvailableTicketForAdmin() {

        when(ticketRepository.findById(10L))
                .thenReturn(Optional.of(ticket));

        Ticket result =
                ticketService.getAvailableTicket(10L, admin);

        assertEquals(ticket, result);

        verify(ticketRepository).findById(10L);

        verify(ticketRepository, never())
                .findByIdAndAuthorId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("TICK-13: Доступ владельца к заявке")
    void getAvailableTicketForOwner() {

        when(ticketRepository.findByIdAndAuthorId(10L, 1L))
                .thenReturn(Optional.of(ticket));

        Ticket result =
                ticketService.getAvailableTicket(10L, user);

        assertEquals(ticket, result);

        verify(ticketRepository)
                .findByIdAndAuthorId(10L, 1L);

        verify(ticketRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("TICK-14: Запрет доступа к чужой заявке")
    void getAvailableTicketForForeignUser() {

        User anotherUser = new User();

        anotherUser.setId(3L);
        anotherUser.setRole(Role.USER);

        when(ticketRepository.findByIdAndAuthorId(10L, 3L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.getAvailableTicket(
                        10L,
                        anotherUser
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(ticketRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("TICK-15: Получение списка категорий")
    void getCategoriesSuccess() {

        when(categoryRepository.findAll())
                .thenReturn(List.of(category));

        List<Category> result =
                ticketService.getCategories();

        assertEquals(1, result.size());
        assertEquals(category, result.get(0));

        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("TICK-16: Сортировка заявок ASC и DESC")
    void findTicketsSorting() {

        TicketFilterDto filter = new TicketFilterDto();

        Sort ascending =
                Sort.by(Sort.Direction.ASC, "createdAt", "id");

        Sort descending =
                Sort.by(Sort.Direction.DESC, "createdAt", "id");

        when(ticketRepository.findAll(
                any(Specification.class),
                any(Sort.class)
        )).thenReturn(List.of(ticket));

        filter.setSort("asc");

        List<Ticket> ascendingResult =
                ticketService.findTickets(filter);

        assertEquals(List.of(ticket), ascendingResult);

        verify(ticketRepository).findAll(
                any(Specification.class),
                eq(ascending)
        );

        filter.setSort("desc");

        List<Ticket> descendingResult =
                ticketService.findTickets(filter);

        assertEquals(List.of(ticket), descendingResult);

        verify(ticketRepository).findAll(
                any(Specification.class),
                eq(descending)
        );
    }
}