package com.example.eventservice.repository;

import com.example.eventservice.entity.Category;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
  Optional<Category> findByName(String name);
}
