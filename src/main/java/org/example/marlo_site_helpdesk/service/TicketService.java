package org.example.marlo_site_helpdesk.service;

import jakarta.persistence.criteria.Predicate;
import org.example.marlo_site_helpdesk.models.Ticket;
import org.example.marlo_site_helpdesk.repository.TicketRepository;
import org.example.marlo_site_helpdesk.dto.TicketFilterDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
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
}