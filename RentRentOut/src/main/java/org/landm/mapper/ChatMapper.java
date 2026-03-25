package org.landm.mapper;


import org.landm.dto.chat.ConversationPreviewDto;
import org.landm.dto.chat.MessageDto;
import org.landm.entity.Conversation;
import org.landm.entity.Enums.MessageType;
import org.landm.entity.Message;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.landm.repository.MessageRepository;
import org.landm.repository.RentalContractRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMapper {


    private final UserMapper userMapper;
    private final MessageRepository messageRepository;
    private final RentalContractRepository rentalContractRepository;

    public ChatMapper(UserMapper userMapper, MessageRepository messageRepository,
                      RentalContractRepository rentalContractRepository) {
        this.userMapper = userMapper;
        this.messageRepository = messageRepository;
        this.rentalContractRepository = rentalContractRepository;
    }

    public MessageDto toMessageDto(Message m){
        if (m == null) return null;

        MessageDto dto = new MessageDto();
        dto.setId(m.getId());
        dto.setConversationId(m.getConversation().getId());
        dto.setSenderId(m.getSender().getId());
        dto.setContent(m.getContent());
        dto.setRead(m.isRead());
        dto.setMessageType(m.getMessageType().name());
        dto.setRelatedContractId(m.getRelatedContractId());
        dto.setCreatedAt(m.getCreatedAt());

        if (m.getMessageType() == MessageType.CONTRACT_REQUEST && m.getRelatedContractId() != null) {
            rentalContractRepository.findById(m.getRelatedContractId()).ifPresent(contract -> {
                dto.setContractAdTitle(contract.getAd().getTitle());
                dto.setContractStartDate(contract.getStartDate().toString());
                dto.setContractEndDate(contract.getEndDate().toString());
                dto.setContractTotalPrice(contract.getAgreedPrice()
                        .multiply(java.math.BigDecimal.valueOf(contract.getAmount())));
                dto.setContractCurrency(contract.getCurrency().name());
            });
        }

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
        List<Message> messages = c.getMessages();
        if(!messages.isEmpty()) {
            Message lastMessage = messages.get(messages.size() - 1);
            dto.setLastMessagePreview(lastMessage.getContent());
        }


        long unread = messageRepository.countUnreadForConversation(c.getId(), myUserId);
        dto.setUnreadCount((int) unread);
        return dto;
    }
}
