package org.example.marlo_site_helpdesk.service;

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
        return ticketRepository.getStatistics();
    }
}