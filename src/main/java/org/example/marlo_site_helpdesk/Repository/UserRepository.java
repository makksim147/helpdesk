package org.example.marlo_site_helpdesk.Repository;

import org.example.marlo_site_helpdesk.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}