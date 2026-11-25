package org.example.marlo_site_helpdesk.service;

import jakarta.persistence.criteria.Predicate;
import org.example.marlo_site_helpdesk.models.Ticket;
import org.example.marlo_site_helpdesk.repository.TicketRepository;
import org.example.marlo_site_helpdesk.dto.TicketFilterDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.marlo_site_helpdesk.models.*;
import org.example.marlo_site_helpdesk.repository.CategoryRepository;
import org.example.marlo_site_helpdesk.repository.UserRepository;
import org.example.marlo_site_helpdesk.dto.TicketCreateDto;
import org.example.marlo_site_helpdesk.dto.TicketUpdateDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TicketService(
            TicketRepository ticketRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Ticket> findTickets(TicketFilterDto filter) {

        Specification<Ticket> specification = (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSearch() != null
                    && !filter.getSearch().isBlank()) {

                String search = filter.getSearch()
                        .trim()
                        .toLowerCase(Locale.ROOT);

                String escapedSearch = search
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_");

                Predicate titlePredicate = cb.like(
                        cb.lower(root.get("title")),
                        "%" + escapedSearch + "%",
                        '\\'
                );

                try {

                    Long id = Long.parseLong(search);

                    Predicate idPredicate = cb.equal(
                            root.get("id"),
                            id
                    );

                    predicates.add(
                            cb.or(titlePredicate, idPredicate)
                    );

                } catch (NumberFormatException e) {

                    predicates.add(titlePredicate);
                }
            }

            if (filter.getStatus() != null) {

                predicates.add(
                        cb.equal(
                                root.get("status"),
                                filter.getStatus()
                        )
                );
            }

            if (filter.getPriority() != null) {

                predicates.add(
                        cb.equal(
                                root.get("priority"),
                                filter.getPriority()
                        )
                );
            }

            if (filter.getCategoryId() != null) {

                predicates.add(
                        cb.equal(
                                root.get("category").get("id"),
                                filter.getCategoryId()
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };

        Sort.Direction direction = Sort.Direction.DESC;

        if ("asc".equals(filter.getSort())) {
            direction = Sort.Direction.ASC;
        }

        Sort sort = Sort.by(direction, "createdAt", "id");

        return ticketRepository.findAll(specification, sort);
    }

    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Ticket> getMyTickets(String email) {

        return ticketRepository
                .findByAuthorEmailOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public Ticket getOwnTicket(Long id, String email) {

        return ticketRepository
                .findByIdAndAuthorEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Заявка не найдена"
                ));
    }

    @Transactional(readOnly = true)
    public Ticket getTicket(Long id) {

        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Заявка не найдена"
                ));
    }

    public enum CreateResult {
        SUCCESS,
        CATEGORY_NOT_FOUND
    }

    @Transactional
    public CreateResult createTicket(
            TicketCreateDto dto,
            String email) {

        Optional<Category> category =
                categoryRepository.findById(dto.getCategoryId());

        if (category.isEmpty()) {
            return CreateResult.CATEGORY_NOT_FOUND;
        }

        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED
                ));

        Ticket ticket = new Ticket();

        ticket.setTitle(dto.getTitle().trim());
        ticket.setDescription(dto.getDescription().trim());
        ticket.setCategory(category.get());
        ticket.setPriority(dto.getPriority());
        ticket.setAuthor(author);
        ticket.setStatus(Status.NEW);

        ticketRepository.save(ticket);

        return CreateResult.SUCCESS;
    }

    @Transactional
    public void updateTicket(Long id, TicketUpdateDto dto) {

        Ticket ticket = getTicket(id);

        ticket.setStatus(dto.getStatus());
        ticket.setPriority(dto.getPriority());

        ticketRepository.save(ticket);
    }

    @Transactional
    public void closeTicket(Long id) {

        Ticket ticket = getTicket(id);

        if (ticket.getStatus() != Status.DONE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Закрыть можно только выполненную заявку"
            );
        }

        ticket.setStatus(Status.CLOSED);

        ticketRepository.save(ticket);
    }

    
}