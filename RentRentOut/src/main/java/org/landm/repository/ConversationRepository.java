package org.landm.repository;

import org.landm.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c " +
            "FROM Conversation  c " +
            "where c.participantOne.id = :userId OR c.participantTwo.id = :userId" +
            " order by c.updatedAt DESC ")
    Page<Conversation> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c " +
            "FROM Conversation c " +
            "WHERE c.ad.id = :adId AND" +
            "((c.participantOne.id = :p1 AND c.participantTwo.id = :p2) OR " +
            "(c.participantOne.id = :p2 AND c.participantTwo.id = :p1))  ")
    Optional<Conversation> findExistingConversation(@Param("adId") Long adId, @Param("p1") Long p1, @Param("p2") Long p2);
}
