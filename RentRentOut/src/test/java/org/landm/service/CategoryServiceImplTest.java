package org.landm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.landm.entity.Category;
import org.landm.mapper.CategoryMapper;
import org.landm.repository.CategoryRepository;
import org.landm.service.impl.CategoryServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void findAllSubCategoryId_leafCategory_returnsOnlyItself() {
        Category leaf = new Category();
        leaf.setId(10L);
        leaf.setName("Leaf");

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(leaf));
        when(categoryRepository.findByParentId(10L)).thenReturn(List.of());

        List<Long> result = categoryService.findAllSubCategoryId(10L);

        assertThat(result).containsExactly(10L);
    }

    @Test
    void findAllSubCategoryId_parentWithChildren_returnsAllDescendants() {
        Category parent = new Category();
        parent.setId(1L);

        Category child1 = new Category();
        child1.setId(2L);

        Category child2 = new Category();
        child2.setId(3L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(child1, child2));
        when(categoryRepository.findByParentId(2L)).thenReturn(List.of());
        when(categoryRepository.findByParentId(3L)).thenReturn(List.of());

        List<Long> result = categoryService.findAllSubCategoryId(1L);

        assertThat(result).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void findAllSubCategoryId_nonExistentCategory_throws() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findAllSubCategoryId(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void get_nonExistentId_throws() {
        when(categoryRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.get(123L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
