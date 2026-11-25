package org.example.marlo_site_helpdesk.repository;

import org.example.marlo_site_helpdesk.models.Ticket;
import org.example.marlo_site_helpdesk.dto.TicketStatsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends
        JpaRepository<Ticket, Long>,
        JpaSpecificationExecutor<Ticket> {

    List<Ticket> findByAuthorEmailOrderByCreatedAtDesc(String email);

    Optional<Ticket> findByIdAndAuthorEmail(Long id, String email);

    @Query("""
            SELECT new org.example.marlo_site_helpdesk.dto.TicketStatsDto(
                COUNT(t),
                COUNT(CASE WHEN t.status = org.example.marlo_site_helpdesk.models.Status.NEW THEN 1 ELSE NULL END),
                COUNT(CASE WHEN t.status = org.example.marlo_site_helpdesk.models.Status.IN_PROGRESS THEN 1 ELSE NULL END),
                COUNT(CASE WHEN t.status = org.example.marlo_site_helpdesk.models.Status.DONE THEN 1 ELSE NULL END),
                COUNT(CASE WHEN t.status = org.example.marlo_site_helpdesk.models.Status.CLOSED THEN 1 ELSE NULL END),
                COUNT(CASE WHEN t.priority = org.example.marlo_site_helpdesk.models.Priority.HIGH THEN 1 ELSE NULL END),
                COUNT(CASE WHEN t.priority = org.example.marlo_site_helpdesk.models.Priority.CRITICAL THEN 1 ELSE NULL END)
            )
            FROM Ticket t
            """)
    TicketStatsDto getStatistics();
}