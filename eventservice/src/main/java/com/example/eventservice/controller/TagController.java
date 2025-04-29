package com.example.eventservice.controller;

import com.example.eventservice.request.TagRequest;
import com.example.eventservice.response.TagResponse;
import com.example.eventservice.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/all")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        List<TagResponse> tags = tagService.findAll();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable UUID tagId) {
        TagResponse tag = tagService.findById(tagId);
        return ResponseEntity.ok(tag);
    }

    @PostMapping("/create")
    public ResponseEntity<TagResponse> createTag(
            @RequestBody
            TagRequest tagRequest) {
        TagResponse createdTag = tagService.create(tagRequest);
        return ResponseEntity.status(201).body(createdTag);
    }

    @PutMapping("/update/{tagId}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable UUID tagId,
            @RequestBody
            TagRequest tagRequest) {
        TagResponse updatedTag = tagService.update(tagId, tagRequest);
        return ResponseEntity.ok(updatedTag);
    }

    @DeleteMapping("/delete/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID tagId) {
        tagService.delete(tagId);
        return ResponseEntity.noContent().build();
    }
}
