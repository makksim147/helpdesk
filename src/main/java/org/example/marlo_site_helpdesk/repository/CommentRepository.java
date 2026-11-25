package org.example.marlo_site_helpdesk.repository;

import org.example.marlo_site_helpdesk.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);
}