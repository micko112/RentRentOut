package org.landm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.landm.entity.CategorySuggestionLog;
import org.landm.repository.CategorySuggestionLogRepository;
import org.landm.service.CategorySuggestionLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CategorySuggestionLogServiceImpl implements CategorySuggestionLogService {

    private static final int MAX_QUERY_LEN = 500;
    private static final int MAX_JSON_LEN = 2000;
    private static final int ACCEPT_WINDOW_MINUTES = 30;

    private final CategorySuggestionLogRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CategorySuggestionLogServiceImpl(CategorySuggestionLogRepository repository) {
        this.repository = repository;
    }

    @Async
    @Override
    public void logSuggestion(String queryText, List<Map<String, Object>> suggestions, Long userId) {
        try {
            String query = queryText == null ? "" : queryText;
            if (query.length() > MAX_QUERY_LEN) {
                query = query.substring(0, MAX_QUERY_LEN);
            }

            String json = null;
            Long topCategoryId = null;
            Double topConfidence = null;

            if (suggestions != null && !suggestions.isEmpty()) {
                try {
                    json = objectMapper.writeValueAsString(suggestions);
                    if (json.length() > MAX_JSON_LEN) {
                        json = json.substring(0, MAX_JSON_LEN);
                    }
                } catch (JsonProcessingException e) {
                    log.warn("Ne mogu da serializujem suggestions u JSON: {}", e.getMessage());
                }

                Map<String, Object> top = suggestions.get(0);
                Object idObj = top.get("category_id");
                Object confObj = top.get("confidence");
                if (idObj instanceof Number n) topCategoryId = n.longValue();
                if (confObj instanceof Number n) topConfidence = n.doubleValue();
            }

            repository.save(new CategorySuggestionLog(query, json, topCategoryId, topConfidence, userId));
        } catch (Exception e) {
            log.warn("Suggestion log nije sacuvan: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void markAccepted(Long userId, Long chosenCategoryId) {
        if (userId == null || chosenCategoryId == null) return;
        try {
            LocalDateTime since = LocalDateTime.now().minusMinutes(ACCEPT_WINDOW_MINUTES);
            List<CategorySuggestionLog> recent =
                    repository.findByUserIdAndCreatedAtAfterAndAcceptedCategoryIdIsNull(userId, since);
            if (recent.isEmpty()) return;
            for (CategorySuggestionLog entry : recent) {
                entry.setAcceptedCategoryId(chosenCategoryId);
            }
            repository.saveAll(recent);
        } catch (Exception e) {
            log.warn("markAccepted nije uspeo (userId={}, categoryId={}): {}", userId, chosenCategoryId, e.getMessage());
        }
    }
}
