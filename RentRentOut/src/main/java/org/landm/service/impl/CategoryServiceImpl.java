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
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryDto create(CreateCategoryRequestDto req, long userId) {
        Category categoryToCreate = new Category();
        categoryToCreate.setName(req.getName());

        if(req.getParentId() != null){
            Category parent = categoryRepository.findById(req.getParentId()).orElseThrow(() -> new RuntimeException("Parent category not found"));
            categoryToCreate.setParent(parent);
        }

        return categoryMapper.toDto(categoryRepository.save(categoryToCreate));

    }

    @Override
    public List<CategoryDto> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public CategoryDto get(long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }

    @Override
    public List<Long> findAllSubCategoryId(Long parentId) {
        Category parent = categoryRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Category not found"));
        List<Long> allIds = new ArrayList<>();
        collectAllChildIds(parent, allIds);
        return allIds;

    }
    public void collectAllChildIds(Category category, List<Long> allIds){
        allIds.add(category.getId());
        List<Category> children = categoryRepository.findByParentId(category.getId());

        for (Category child : children){
            collectAllChildIds(child, allIds);
        }
    }


}
