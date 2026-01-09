package org.landm.service;

import org.landm.dto.CategoryDto;
import org.landm.dto.CreateCategoryRequestDto;
import org.landm.entity.Category;

public interface CategoryService {
    public CategoryDto create(CreateCategoryRequestDto req, String authHeader);
}
