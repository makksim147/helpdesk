package org.example.marlo_site_helpdesk.repository;

import org.example.marlo_site_helpdesk.models.Ticket;
import org.example.marlo_site_helpdesk.models.Status;
import org.example.marlo_site_helpdesk.models.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends
        JpaRepository<Ticket, Long>,
        JpaSpecificationExecutor<Ticket> {

    List<Ticket> findByAuthorEmailOrderByCreatedAtDesc(String email);

    Optional<Ticket> findByIdAndAuthorEmail(Long id, String email);

    List<Ticket> findAllByOrderByCreatedAtDesc();

    long countByStatus(Status status);

    long countByPriority(Priority priority);
}