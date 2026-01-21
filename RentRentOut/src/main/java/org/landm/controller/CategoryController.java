package org.landm.controller;


import jakarta.validation.Valid;
import org.landm.dto.CategoryDto;
import org.landm.dto.requestDto.CreateCategoryRequestDto;
import org.landm.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/categories")
public class CategoryController {
        private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryDto> post(@Valid @RequestBody CreateCategoryRequestDto req,
                                            Authentication auth) {
        long userId = Long.parseLong(auth.getName());
        return new ResponseEntity<>(categoryService.create(req, userId), HttpStatus.CREATED);

    }

}
