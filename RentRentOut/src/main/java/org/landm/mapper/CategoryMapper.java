package org.landm.mapper;


import org.landm.dto.CategoryDto;
import org.landm.entity.Category;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CategoryMapper {
    public CategoryDto toDto(Category category){
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(resolveName(category));
        if (category.getParent() != null) {
            categoryDto.setParentId(category.getParent().getId());
        }
        return categoryDto;
    }

    private String resolveName(Category category) {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            String en = category.getNameEn();
            if (en != null && !en.isBlank()) {
                return en;
            }
        }
        return category.getName();
    }

}
