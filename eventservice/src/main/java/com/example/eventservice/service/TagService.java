package com.example.eventservice.service;

import com.example.eventservice.entity.Tag;
import com.example.eventservice.repository.TagRepository;
import com.example.eventservice.request.TagRequest;
import com.example.eventservice.response.TagResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TagService {
  private final TagRepository tagRepository;

  public TagResponse create(TagRequest tagRequest) {
    var tag = tagRepository.findByNameIgnoreCase(tagRequest.name());

    if (tag.isPresent()) {
      throw new EntityExistsException("Tag already exists");
    }

    var newTag = tagRepository.save(Tag.builder().name(tagRequest.name()).build());

    return new TagResponse(newTag.getId(), newTag.getName());
  }

  public Tag createIfNotExists(String name) {
    String normalized = name.trim().toLowerCase();

    return tagRepository
        .findByNameIgnoreCase(normalized)
        .orElseGet(() -> tagRepository.save(Tag.builder().name(normalized).build()));
  }

  public TagResponse findById(UUID id) {
    var tag =
        tagRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Tag not found"));
    return new TagResponse(tag.getId(), tag.getName());
  }

  public List<TagResponse> findAll() {
    return tagRepository.findAll().stream()
        .map(tag -> new TagResponse(tag.getId(), tag.getName()))
        .toList();
  }

  public TagResponse update(UUID tagId, TagRequest tagRequest) {
    var tag =
        tagRepository
            .findById(tagId)
            .orElseThrow(() -> new EntityNotFoundException("Tag not found"));

    var existingTag = tagRepository.findByNameIgnoreCase(tagRequest.name());

    if (existingTag.isPresent() && !existingTag.get().getId().equals(tagId)) {
      throw new EntityExistsException("Tag with this name already exists");
    }

    tag.setName(tagRequest.name());

    var updatedTag = tagRepository.save(tag);

    return new TagResponse(updatedTag.getId(), updatedTag.getName());
  }

  public void delete(UUID id) {
    var tag =
        tagRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Tag not found"));
    tagRepository.delete(tag);
  }
}
