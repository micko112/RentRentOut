package org.landm.repository;

import org.landm.entity.CategorySuggestionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CategorySuggestionLogRepository extends JpaRepository<CategorySuggestionLog, Long> {

    List<CategorySuggestionLog> findByUserIdAndCreatedAtAfterAndAcceptedCategoryIdIsNull(
            Long userId, LocalDateTime since);
}
