package org.landm.controller;


import jakarta.validation.Valid;
import org.landm.dto.CategoryDto;
import org.landm.dto.requestDto.CreateCategoryRequestDto;
import org.landm.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        CategoryDto category = categoryService.get(id);
        return ResponseEntity.ok(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryDto> post(@Valid @RequestBody CreateCategoryRequestDto req) {
        return new ResponseEntity<>(categoryService.create(req), HttpStatus.CREATED);
    }
    @GetMapping("/suggest")
    public ResponseEntity<Long> suggestCategory(@RequestParam String title) {
        Long categoryId = categoryService.suggestCategory(title);
        return ResponseEntity.ok(categoryId);
    }

}
