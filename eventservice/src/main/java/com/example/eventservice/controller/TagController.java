package com.example.eventservice.controller;

import com.example.eventservice.request.TagRequest;
import com.example.eventservice.response.TagResponse;
import com.example.eventservice.service.TagService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tags")
@AllArgsConstructor
public class TagController {

  private final TagService tagService;

  @GetMapping("/all")
  public ResponseEntity<List<TagResponse>> getAllTags() {
    List<TagResponse> tags = tagService.findAll();
    return ResponseEntity.ok(tags);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TagResponse> getTagById(@PathVariable Long id) {
    TagResponse tag = tagService.findById(id);
    return ResponseEntity.ok(tag);
  }

  @PostMapping("/create")
  public ResponseEntity<TagResponse> createTag(@RequestBody TagRequest tagRequest) {
    TagResponse createdTag = tagService.create(tagRequest);
    return ResponseEntity.status(201).body(createdTag);
  }

  @PutMapping("/update/{id}")
  public ResponseEntity<TagResponse> updateTag(
      @PathVariable Long id, @RequestBody TagRequest tagRequest) {
    TagResponse updatedTag = tagService.update(id, tagRequest);
    return ResponseEntity.ok(updatedTag);
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
    tagService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
