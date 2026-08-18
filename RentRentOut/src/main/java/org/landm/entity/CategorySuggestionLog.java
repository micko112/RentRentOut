package org.landm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_suggestion_log")
public class CategorySuggestionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query_text", nullable = false, length = 500)
    private String queryText;

    @Column(name = "suggestions_json", length = 2000)
    private String suggestionsJson;

    @Column(name = "top_category_id")
    private Long topCategoryId;

    @Column(name = "top_confidence")
    private Double topConfidence;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "accepted_category_id")
    private Long acceptedCategoryId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CategorySuggestionLog() {}

    public CategorySuggestionLog(String queryText, String suggestionsJson,
                                 Long topCategoryId, Double topConfidence, Long userId) {
        this.queryText = queryText;
        this.suggestionsJson = suggestionsJson;
        this.topCategoryId = topCategoryId;
        this.topConfidence = topConfidence;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public String getQueryText() { return queryText; }
    public String getSuggestionsJson() { return suggestionsJson; }
    public Long getTopCategoryId() { return topCategoryId; }
    public Double getTopConfidence() { return topConfidence; }
    public Long getUserId() { return userId; }
    public Long getAcceptedCategoryId() { return acceptedCategoryId; }
    public void setAcceptedCategoryId(Long acceptedCategoryId) { this.acceptedCategoryId = acceptedCategoryId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
