package org.example.marlo_site_helpdesk.service;

import org.example.marlo_site_helpdesk.dto.TicketStatsDto;
import org.example.marlo_site_helpdesk.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {

        statisticsService =
                new StatisticsService(ticketRepository);
    }

    @Test
    @DisplayName("STAT-01: Получение статистики")
    void getStatisticsSuccess() {

        TicketStatsDto statistics = new TicketStatsDto(
                20,
                5,
                4,
                3,
                2,
                6,
                2
        );

        when(ticketRepository.getStatistics())
                .thenReturn(statistics);

        TicketStatsDto result =
                statisticsService.getStatistics();

        assertNotNull(result);

        assertEquals(20L, result.getTotal());
        assertEquals(5L, result.getNewTickets());
        assertEquals(4L, result.getInProgress());
        assertEquals(3L, result.getDone());
        assertEquals(2L, result.getClosed());
        assertEquals(6L, result.getHigh());
        assertEquals(2L, result.getCritical());

        verify(ticketRepository).getStatistics();

        verifyNoMoreInteractions(ticketRepository);
    }

    @Test
    @DisplayName("STAT-02: Отсутствие заявок")
    void getEmptyStatistics() {

        TicketStatsDto statistics = new TicketStatsDto(
                0, 0, 0, 0, 0, 0, 0
        );

        when(ticketRepository.getStatistics())
                .thenReturn(statistics);

        TicketStatsDto result =
                statisticsService.getStatistics();

        assertNotNull(result);

        assertEquals(0L, result.getTotal());
        assertEquals(0L, result.getNewTickets());
        assertEquals(0L, result.getInProgress());
        assertEquals(0L, result.getDone());
        assertEquals(0L, result.getClosed());
        assertEquals(0L, result.getHigh());
        assertEquals(0L, result.getCritical());

        verify(ticketRepository).getStatistics();

        verifyNoMoreInteractions(ticketRepository);
    }

    @Test
    @DisplayName("STAT-03: Получение актуальной статистики")
    void getUpdatedStatistics() {

        TicketStatsDto initialStatistics =
                new TicketStatsDto(
                        10, 3, 2, 2, 1, 2, 1
                );

        TicketStatsDto updatedStatistics =
                new TicketStatsDto(
                        11, 4, 2, 2, 1, 3, 1
                );

        when(ticketRepository.getStatistics())
                .thenReturn(
                        initialStatistics,
                        updatedStatistics
                );

        TicketStatsDto firstResult =
                statisticsService.getStatistics();

        TicketStatsDto secondResult =
                statisticsService.getStatistics();

        assertEquals(10L, firstResult.getTotal());
        assertEquals(11L, secondResult.getTotal());

        assertEquals(3L, firstResult.getNewTickets());
        assertEquals(4L, secondResult.getNewTickets());

        assertEquals(2L, firstResult.getHigh());
        assertEquals(3L, secondResult.getHigh());

        verify(ticketRepository, times(2))
                .getStatistics();

        verifyNoMoreInteractions(ticketRepository);
    }
}