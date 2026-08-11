package org.landm.dto;

public class CategorySuggestionDto {
    private Long categoryId;
    private double confidence;

    public CategorySuggestionDto() {}

    public CategorySuggestionDto(Long categoryId, double confidence) {
        this.categoryId = categoryId;
        this.confidence = confidence;
    }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}
