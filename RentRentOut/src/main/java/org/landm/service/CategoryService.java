package org.landm.service;

import org.landm.dto.CategoryDto;
import org.landm.dto.requestDto.CreateCategoryRequestDto;

public interface CategoryService {
    public CategoryDto create(CreateCategoryRequestDto req, String authHeader);
    public CategoryDto get(long id);
}
