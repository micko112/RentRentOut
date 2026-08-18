package org.landm.service;

import java.util.List;
import java.util.Map;

public interface CategorySuggestionLogService {
    void logSuggestion(String queryText, List<Map<String, Object>> suggestions, Long userId);

    void markAccepted(Long userId, Long chosenCategoryId);
}
