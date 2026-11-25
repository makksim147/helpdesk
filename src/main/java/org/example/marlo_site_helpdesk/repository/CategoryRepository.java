package org.example.marlo_site_helpdesk.repository;

import org.example.marlo_site_helpdesk.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}