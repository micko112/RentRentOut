package org.landm.service;

import org.landm.dto.CategoryDto;
import org.landm.dto.requestDto.CreateCategoryRequestDto;

import java.util.List;

public interface CategoryService {
    public CategoryDto create(CreateCategoryRequestDto req, long userId);
    public CategoryDto get(long id);
    public List<Long> findAllSubCategoryId(Long parentId);

}
