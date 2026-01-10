package org.landm.controller;


import jakarta.validation.Valid;
import org.landm.dto.CategoryDto;
import org.landm.dto.requestDto.CreateCategoryRequestDto;
import org.landm.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/category")
public class CategoryController {
        private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<CategoryDto> post(@Valid @RequestBody CreateCategoryRequestDto req,
                                            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return new ResponseEntity<>(categoryService.create(req, token), HttpStatus.CREATED);

    }

}
