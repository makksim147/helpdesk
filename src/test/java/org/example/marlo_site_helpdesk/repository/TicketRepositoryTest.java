package org.example.marlo_site_helpdesk.repository;

import jakarta.persistence.EntityManager;
import org.example.marlo_site_helpdesk.dto.TicketFilterDto;
import org.example.marlo_site_helpdesk.dto.TicketStatsDto;
import org.example.marlo_site_helpdesk.models.*;
import org.example.marlo_site_helpdesk.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private User anotherUser;

    private Category technicalCategory;
    private Category accountCategory;

    private Ticket firstTicket;
    private Ticket secondTicket;
    private Ticket thirdTicket;
    private Ticket fourthTicket;
    private Ticket fifthTicket;

    @BeforeEach
    void setUp() {

        user = createUser(
                "Test User",
                "test@example.com"
        );

        anotherUser = createUser(
                "Another User",
                "another@example.com"
        );

        technicalCategory = createCategory(
                "Техническая проблема"
        );

        accountCategory = createCategory(
                "Проблема с аккаунтом"
        );

        firstTicket = createTicket(
                "Проблема с компьютером",
                Status.NEW,
                Priority.HIGH,
                technicalCategory,
                user,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        secondTicket = createTicket(
                "Ошибка входа",
                Status.IN_PROGRESS,
                Priority.CRITICAL,
                accountCategory,
                user,
                LocalDateTime.of(2026, 1, 2, 10, 0)
        );

        thirdTicket = createTicket(
                "Настройка компьютера",
                Status.DONE,
                Priority.MEDIUM,
                technicalCategory,
                anotherUser,
                LocalDateTime.of(2026, 1, 3, 10, 0)
        );

        fourthTicket = createTicket(
                "Восстановление аккаунта",
                Status.CLOSED,
                Priority.LOW,
                accountCategory,
                anotherUser,
                LocalDateTime.of(2026, 1, 4, 10, 0)
        );

        fifthTicket = createTicket(
                "Ошибка программы",
                Status.NEW,
                Priority.HIGH,
                technicalCategory,
                user,
                LocalDateTime.of(2026, 1, 5, 10, 0)
        );

        entityManager.flush();
        entityManager.clear();
    }

    private User createUser(String name, String email) {

        User newUser = new User();

        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPasswordHash("test-password-hash");
        newUser.setRole(Role.USER);

        return userRepository.save(newUser);
    }

    private Category createCategory(String name) {

        Category category = new Category();

        category.setName(name);

        return categoryRepository.save(category);
    }

    private Ticket createTicket(
            String title,
            Status status,
            Priority priority,
            Category category,
            User author,
            LocalDateTime createdAt
    ) {

        Ticket ticket = new Ticket();

        ticket.setTitle(title);
        ticket.setDescription("Тестовое описание заявки");
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setCategory(category);
        ticket.setAuthor(author);

        Ticket savedTicket =
                ticketRepository.saveAndFlush(ticket);

        entityManager.createQuery(
                        "UPDATE Ticket t SET t.createdAt = :date " +
                                "WHERE t.id = :id"
                )
                .setParameter("date", createdAt)
                .setParameter("id", savedTicket.getId())
                .executeUpdate();

        return savedTicket;
    }

    @Test
    @DisplayName("REP-01: Сохранение заявки в базе данных")
    void saveTicketSuccess() {

        Ticket ticket = createTicket(
                "Новая тестовая заявка",
                Status.NEW,
                Priority.MEDIUM,
                technicalCategory,
                user,
                LocalDateTime.of(2026, 1, 6, 10, 0)
        );

        entityManager.flush();
        entityManager.clear();

        Optional<Ticket> result =
                ticketRepository.findById(ticket.getId());

        assertTrue(result.isPresent());

        Ticket savedTicket = result.get();

        assertEquals(
                "Новая тестовая заявка",
                savedTicket.getTitle()
        );

        assertEquals(Status.NEW, savedTicket.getStatus());
        assertEquals(Priority.MEDIUM, savedTicket.getPriority());

        assertEquals(
                user.getId(),
                savedTicket.getAuthor().getId()
        );

        assertEquals(
                technicalCategory.getId(),
                savedTicket.getCategory().getId()
        );
    }

    @Test
    @DisplayName("REP-02: Получение заявок пользователя")
    void findTicketsByAuthor() {

        List<Ticket> result =
                ticketRepository
                        .findByAuthorIdOrderByCreatedAtDesc(
                                user.getId()
                        );

        assertEquals(3, result.size());

        for (Ticket ticket : result) {
            assertEquals(
                    user.getId(),
                    ticket.getAuthor().getId()
            );
        }

        assertEquals(
                fifthTicket.getId(),
                result.get(0).getId()
        );

        assertEquals(
                firstTicket.getId(),
                result.get(2).getId()
        );
    }

    @Test
    @DisplayName("REP-03: Получение собственной заявки")
    void findOwnTicket() {

        Optional<Ticket> result =
                ticketRepository.findByIdAndAuthorId(
                        firstTicket.getId(),
                        user.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                firstTicket.getId(),
                result.get().getId()
        );
    }

    @Test
    @DisplayName("REP-04: Получение чужой заявки запрещено")
    void findForeignTicket() {

        Optional<Ticket> result =
                ticketRepository.findByIdAndAuthorId(
                        firstTicket.getId(),
                        anotherUser.getId()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("REP-05: Поиск по заголовку")
    void searchByTitle() {

        TicketFilterDto filter = new TicketFilterDto();

        filter.setSearch("компьютер");

        List<Ticket> result =
                ticketService.findTickets(filter);

        assertEquals(2, result.size());

        for (Ticket ticket : result) {
            assertTrue(
                    ticket.getTitle().contains("компьютер")
            );
        }
    }

    @Test
    @DisplayName("REP-06: Поиск без учета регистра")
    void searchIgnoreCase() {

        TicketFilterDto filter = new TicketFilterDto();

        filter.setSearch("ОШИБКА");

        List<Ticket> result =
                ticketService.findTickets(filter);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("REP-07: Поиск по номеру заявки")
    void searchById() {

        TicketFilterDto filter = new TicketFilterDto();

        filter.setSearch(
                firstTicket.getId().toString()
        );

        List<Ticket> result =
                ticketService.findTickets(filter);

        assertEquals(1, result.size());

        assertEquals(
                firstTicket.getId(),
                result.get(0).getId()
        );
    }

    @Test
    @DisplayName("REP-08: Фильтрация по статусу")
    void filterByStatus() {

        TicketFilterDto filter = new TicketFilterDto();

        filter.setStatus(Status.NEW);

        List<Ticket> result =
                ticketService.findTickets(filter);

        assertEquals(2, result.size());

        for (Ticket ticket : result) {
            assertEquals(Status.NEW, ticket.getStatus());
        }
    }

    @Test
    @DisplayName("REP-09: Фильтрация по приоритету")
    void filterByPriority() {

        TicketFilterDto filter = new TicketFilterDto();

        filter.setPriority(Priority.HIGH);

        List<Ticket> result =
                ticketService.findTickets(filter);

        assertEquals(2, result.size());

        for (Ticket ticket : result) {
            assertEquals(
                    Priority.HIGH,
                    ticket.getPriority()
            );
        }
    }

    @Test
    @DisplayName("REP-10: Фильтрация по категории")
    void filterByCategory() {

        TicketFilterDto filter = new TicketFilterDto();

        filter.setCategoryId(
                technicalCategory.getId()
        );

        List<Ticket> result =
                ticketService.findTickets(filter);

        assertEquals(3, result.size());

        for (Ticket ticket : result) {
            assertEquals(
                    technicalCategory.getId(),
                    ticket.getCategory().getId()
            );
        }
    }

    @Test
    @DisplayName("REP-11: Комбинированная фильтрация")
    void filterByMultipleParameters() {

        TicketFilterDto filter = new TicketFilterDto();

        filter.setSearch("Ошибка");
        filter.setStatus(Status.NEW);
        filter.setPriority(Priority.HIGH);
        filter.setCategoryId(
                technicalCategory.getId()
        );

        List<Ticket> result =
                ticketService.findTickets(filter);

        assertEquals(1, result.size());

        assertEquals(
                fifthTicket.getId(),
                result.get(0).getId()
        );
    }

    @Test
    @DisplayName("REP-12: Сортировка по возрастанию даты")
    void sortAscending() {

        TicketFilterDto filter = new TicketFilterDto();

        filter.setSort("asc");

        List<Ticket> result =
                ticketService.findTickets(filter);

        assertEquals(5, result.size());

        assertEquals(
                firstTicket.getId(),
                result.get(0).getId()
        );

        assertEquals(
                fifthTicket.getId(),
                result.get(4).getId()
        );

        for (int i = 1; i < result.size(); i++) {

            assertFalse(
                    result.get(i).getCreatedAt().isBefore(
                            result.get(i - 1).getCreatedAt()
                    )
            );
        }
    }

    @Test
    @DisplayName("REP-13: Сортировка по убыванию даты")
    void sortDescending() {

        TicketFilterDto filter = new TicketFilterDto();

        filter.setSort("desc");

        List<Ticket> result =
                ticketService.findTickets(filter);

        assertEquals(5, result.size());

        assertEquals(
                fifthTicket.getId(),
                result.get(0).getId()
        );

        assertEquals(
                firstTicket.getId(),
                result.get(4).getId()
        );

        for (int i = 1; i < result.size(); i++) {

            assertFalse(
                    result.get(i).getCreatedAt().isAfter(
                            result.get(i - 1).getCreatedAt()
                    )
            );
        }
    }

    @Test
    @DisplayName("REP-14: Получение статистики из базы данных")
    void getStatisticsSuccess() {

        TicketStatsDto statistics =
                ticketRepository.getStatistics();

        assertEquals(5L, statistics.getTotal());
        assertEquals(2L, statistics.getNewTickets());
        assertEquals(1L, statistics.getInProgress());
        assertEquals(1L, statistics.getDone());
        assertEquals(1L, statistics.getClosed());
        assertEquals(2L, statistics.getHigh());
        assertEquals(1L, statistics.getCritical());
    }

    @Test
    @DisplayName("REP-15: Статистика пустой базы данных")
    void getEmptyStatistics() {

        ticketRepository.deleteAll();

        entityManager.flush();
        entityManager.clear();

        TicketStatsDto statistics =
                ticketRepository.getStatistics();

        assertEquals(0L, statistics.getTotal());
        assertEquals(0L, statistics.getNewTickets());
        assertEquals(0L, statistics.getInProgress());
        assertEquals(0L, statistics.getDone());
        assertEquals(0L, statistics.getClosed());
        assertEquals(0L, statistics.getHigh());
        assertEquals(0L, statistics.getCritical());
    }

    @Test
    @DisplayName("REP-16: Поиск несуществующей заявки")
    void searchNonExistingTicket() {

        TicketFilterDto filter = new TicketFilterDto();

        filter.setSearch("Несуществующая заявка");

        List<Ticket> result =
                ticketService.findTickets(filter);

        assertTrue(result.isEmpty());
    }
}