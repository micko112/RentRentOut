package org.landm.service.impl;

import org.landm.dto.CategoryDto;
import org.landm.dto.requestDto.CreateCategoryRequestDto;
import org.landm.entity.Category;
import org.landm.entity.Item;
import org.landm.mapper.CategoryMapper;
import org.landm.repository.CategoryRepository;
import org.landm.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryDto create(CreateCategoryRequestDto req, String authHeader) {
        Category categoryToCreate = new Category();
        categoryToCreate.setName(req.getName());

        if(req.getParentId() != null){
            Category parent = categoryRepository.findById(req.getParentId()).orElseThrow(() -> new RuntimeException("Parent category not found"));
            categoryToCreate.setParent(parent);
        }

        return categoryMapper.toDto(categoryRepository.save(categoryToCreate));

    }
}
