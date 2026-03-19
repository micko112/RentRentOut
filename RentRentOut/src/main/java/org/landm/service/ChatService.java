package org.landm.service;

import org.landm.dto.chat.ConversationPreviewDto;
import org.landm.dto.chat.MessageDto;
import org.landm.dto.chat.SendMessageRequestDto;
import org.landm.entity.RentalContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    MessageDto sendMessage(SendMessageRequestDto request, Long senderId);

    Page<ConversationPreviewDto> getMyConversations(Long myUserId, Pageable pageable);

    Page<MessageDto> getMessagesForConversation(Long conversationId, Long myUserId, Pageable pageable);

    void sendSystemMessage(Long adId, Long lesseeId, Long lessorId, String content, Long actorId);

    void sendContractRequestMessage(RentalContract contract);

    long getUnreadCount(Long userId);
}
