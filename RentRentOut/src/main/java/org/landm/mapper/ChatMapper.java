package org.landm.mapper;


import org.landm.dto.chat.ConversationPreviewDto;
import org.landm.dto.chat.MessageDto;
import org.landm.entity.Conversation;
import org.landm.entity.Message;
import org.landm.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChatMapper {


    private final UserMapper userMapper;

    public ChatMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public MessageDto toMessageDto(Message m){
        if (m == null) return null;

        MessageDto dto = new MessageDto();
        dto.setId(m.getId());
        dto.setConversationId(m.getConversation().getId());
        dto.setSenderId(m.getSender().getId());
        dto.setContent(m.getContent());
        dto.setRead(m.isRead());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
    public ConversationPreviewDto toDto(Conversation c, Long myUserId){
        if(c == null) return null;

        ConversationPreviewDto dto = new ConversationPreviewDto();
        dto.setAdId(c.getAd().getId());
        dto.setId(c.getId());
        dto.setAdTitle(c.getAd().getTitle());
        if (c.getAd().getImages() != null && !c.getAd().getImages().isEmpty()) {
            dto.setAdThumbnail(c.getAd().getImages().get(0));
        }
        User otherParticipant = c.getParticipantOne().getId().equals(myUserId) ? c.getParticipantTwo() : c.getParticipantOne();

        dto.setOtherParticipant(userMapper.toUserShortDto(otherParticipant));
        dto.setUpdatedAt(c.getUpdatedAt());
        List<Message> messages = new ArrayList<>();
        messages= c.getMessages();
        Message lastMessage = messages.get(messages.size()-1);
        dto.setLastMessagePreview(lastMessage.getContent());

        dto.setUnreadCount(0);
        return dto;
    }
}
