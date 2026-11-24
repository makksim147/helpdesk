package org.example.marlo_site_helpdesk.Repository;

import org.example.marlo_site_helpdesk.Models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}