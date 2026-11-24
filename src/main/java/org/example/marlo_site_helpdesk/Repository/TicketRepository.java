package org.example.marlo_site_helpdesk.Repository;

import org.example.marlo_site_helpdesk.Models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByAuthorEmailOrderByCreatedAtDesc(String email);
}