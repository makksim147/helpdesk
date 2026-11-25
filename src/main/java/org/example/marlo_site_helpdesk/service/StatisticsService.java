package org.example.marlo_site_helpdesk.service;

import org.example.marlo_site_helpdesk.models.Priority;
import org.example.marlo_site_helpdesk.models.Status;
import org.example.marlo_site_helpdesk.repository.TicketRepository;
import org.example.marlo_site_helpdesk.dto.TicketStatsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatisticsService {

    private final TicketRepository ticketRepository;

    public StatisticsService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public TicketStatsDto getStatistics() {

        long total = ticketRepository.count();

        long newTickets =
                ticketRepository.countByStatus(Status.NEW);

        long inProgress =
                ticketRepository.countByStatus(Status.IN_PROGRESS);

        long done =
                ticketRepository.countByStatus(Status.DONE);

        long closed =
                ticketRepository.countByStatus(Status.CLOSED);

        long high =
                ticketRepository.countByPriority(Priority.HIGH);

        long critical =
                ticketRepository.countByPriority(Priority.CRITICAL);

        return new TicketStatsDto(
                total,
                newTickets,
                inProgress,
                done,
                closed,
                high,
                critical
        );
    }
}