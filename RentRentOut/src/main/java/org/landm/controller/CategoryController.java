package org.landm.controller;


import jakarta.validation.Valid;
import org.landm.dto.CategoryDto;
import org.landm.dto.requestDto.CreateCategoryRequestDto;
import org.landm.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/categories")
public class CategoryController {
        private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable long id) {
        CategoryDto category = categoryService.get(id); // Ovu metodu već imaš u servisu
        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> post(@Valid @RequestBody CreateCategoryRequestDto req,
                                            Authentication auth) {
        long userId = Long.parseLong(auth.getName());
        return new ResponseEntity<>(categoryService.create(req, userId), HttpStatus.CREATED);

    }

}
