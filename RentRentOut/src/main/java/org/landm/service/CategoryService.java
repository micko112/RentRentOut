package org.landm.service;

import org.landm.dto.CategoryDto;
import org.landm.dto.requestDto.CreateCategoryRequestDto;

import java.util.List;

public interface CategoryService {
    public CategoryDto create(CreateCategoryRequestDto req);
    public CategoryDto get(Long id);
    public List<Long> findAllSubCategoryId(Long parentId);
    public List<CategoryDto> getAll();
    public Long suggestCategory(String title);

}
