package com.example.eventservice.service;

import com.example.eventservice.entity.Category;
import com.example.eventservice.repository.CategoryRepository;
import com.example.eventservice.request.CategoryRequest;
import com.example.eventservice.response.CategoryResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Cacheable("categories")
  public List<CategoryResponse> findAll() {
    return categoryRepository.findAll().stream()
        .map(category -> new CategoryResponse(category.getId(), category.getName()))
        .toList();
  }

  @Cacheable(value = "category", key = "#id")
  public CategoryResponse findById(UUID id) {
    return categoryRepository
        .findById(id)
        .map(category -> new CategoryResponse(category.getId(), category.getName()))
        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
  }

  @CacheEvict(value = {"categories", "category"}, allEntries = true)
  public CategoryResponse createCategory(CategoryRequest newCategory) {
    if (categoryRepository.findByName(newCategory.name()).isPresent()) {
      throw new EntityExistsException("Category already exists");
    }

    var categoryDb = Category.builder().name(newCategory.name()).build();

    categoryRepository.save(categoryDb);

    return new CategoryResponse(categoryDb.getId(), categoryDb.getName());
  }

  @CacheEvict(value = {"categories", "category"}, allEntries = true)
  public CategoryResponse updateCategory(UUID id, CategoryRequest updatedCategory) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));

    if (updatedCategory.name().equals(category.getName())) {
      throw new EntityExistsException("Category already exists");
    }

    category.setName(updatedCategory.name());
    categoryRepository.save(category);

    return new CategoryResponse(category.getId(), category.getName());
  }

  @CacheEvict(value = {"categories", "category"}, allEntries = true)
  public void deleteCategory(UUID id) {
    if (!categoryRepository.existsById(id)) {
      throw new EntityNotFoundException("Category not found");
    }
    categoryRepository.deleteById(id);
  }
}
