package org.landm.repository;

import org.landm.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);

    @Modifying
    @Query("update Message m set m.isRead = true where m.conversation.id = :convId AND m.sender.id != :myId AND m.isRead = false")
    void markMessageAsRead(@Param("convId") Long convId, @Param("myId") Long myId);
}
