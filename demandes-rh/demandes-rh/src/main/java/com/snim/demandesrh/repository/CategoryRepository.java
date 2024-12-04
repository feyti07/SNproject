package com.snim.demandesrh.repository;

import com.snim.demandesrh.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
