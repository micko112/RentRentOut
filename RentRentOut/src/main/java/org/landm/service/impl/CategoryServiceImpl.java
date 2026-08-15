package org.landm.service.impl;

import org.landm.dto.CategoryDto;
import org.landm.dto.CategorySuggestionDto;
import org.landm.dto.requestDto.CreateCategoryRequestDto;
import org.landm.entity.Category;
import org.landm.mapper.CategoryMapper;
import org.landm.repository.CategoryRepository;
import org.landm.service.CategoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    // FastAPI/Uvicorn puca ("Invalid HTTP request received") kad JDK HttpClient
    // pokušava HTTP/2 upgrade preko HTTP/1.1 — telo se izgubi i backend dobija 422.
    // Force HTTP/1.1 na JDK klijentu rešava problem bez dodatnih dependency-ja.
    private final RestClient restClient = RestClient.builder()
            .requestFactory(new JdkClientHttpRequestFactory(
                    HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()
            ))
            .build();

    @Value("${ai.service.url}")
    private String aiServiceUrl;

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
    @Override
    public List<Long> suggestCategory(String title) {
        Map response = callPredictCategory(title);
        if (response != null && response.containsKey("predicted_category_ids")) {
            List<Number> ids = (List<Number>) response.get("predicted_category_ids");
            return ids.stream().map(Number::longValue).toList();
        }
        return List.of();
    }

    @Override
    public List<CategorySuggestionDto> suggestCategoryDetailed(String title) {
        Map response = callPredictCategory(title);
        if (response != null && response.containsKey("suggestions")) {
            List<Map<String, Object>> raw = (List<Map<String, Object>>) response.get("suggestions");
            return raw.stream()
                    .map(m -> new CategorySuggestionDto(
                            ((Number) m.get("category_id")).longValue(),
                            ((Number) m.get("confidence")).doubleValue()
                    ))
                    .toList();
        }
        return List.of();
    }

    private Map callPredictCategory(String title) {
        Map<String, String> body = new HashMap<>();
        body.put("title", title);
        try {
            return restClient.post()
                    .uri(aiServiceUrl + "/api/predict-category")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            System.out.println("AI Service je trenutno nedostupan: " + e.getMessage());
            return null;
        }
    }

}
