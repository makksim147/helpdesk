package org.example.marlo_site_helpdesk.repository;

import org.example.marlo_site_helpdesk.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}