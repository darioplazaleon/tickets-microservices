package com.example.eventservice.controller;

import com.example.eventservice.request.CategoryRequest;
import com.example.eventservice.response.CategoryResponse;
import com.example.eventservice.service.CategoryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping("/all")
  public ResponseEntity<List<CategoryResponse>> getAllCategories() {
    List<CategoryResponse> categories = categoryService.findAll();
    return ResponseEntity.ok(categories);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
    CategoryResponse category = categoryService.findById(id);
    return ResponseEntity.ok(category);
  }

  @PostMapping("/create")
  public ResponseEntity<CategoryResponse> createCategory(
      @RequestBody CategoryRequest categoryRequest) {
    CategoryResponse category = categoryService.createCategory(categoryRequest);
    return ResponseEntity.status(201).body(category);
  }

  @PutMapping("/update/{id}")
  public ResponseEntity<CategoryResponse> updateCategory(
      @PathVariable UUID id, @RequestBody CategoryRequest categoryRequest) {
    CategoryResponse category = categoryService.updateCategory(id, categoryRequest);
    return ResponseEntity.ok(category);
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }
}
