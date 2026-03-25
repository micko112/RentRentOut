package org.landm.service.impl;

import org.landm.dto.CategoryDto;
import org.landm.dto.requestDto.CreateCategoryRequestDto;
import org.landm.entity.Category;
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
    public CategoryDto create(CreateCategoryRequestDto req) {
        Category categoryToCreate = new Category();
        categoryToCreate.setName(req.getName());

        if(req.getParentId() != null){
            Category parent = categoryRepository.findById(req.getParentId()).orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
            categoryToCreate.setParent(parent);
        }

        return categoryMapper.toDto(categoryRepository.save(categoryToCreate));

    }

    @Override
    public List<CategoryDto> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }
    @Override
    public CategoryDto get(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        return categoryMapper.toDto(category);
    }

    @Override
    public List<Long> findAllSubCategoryId(Long parentId) {
        Category parent = categoryRepository.findById(parentId).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        List<Long> allIds = new ArrayList<>();
        collectAllChildIds(parent, allIds);
        return allIds;

    }
    private void collectAllChildIds(Category category, List<Long> allIds){
        allIds.add(category.getId());
        List<Category> children = categoryRepository.findByParentId(category.getId());

        for (Category child : children){
            collectAllChildIds(child, allIds);
        }
    }


}
