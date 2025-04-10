package com.example.eventservice.repository;

import com.example.eventservice.entity.Tag;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
  Optional<Tag> findByNameIgnoreCase(String name);
}
