package org.landm.service.impl;


import jakarta.transaction.Transactional;
import org.landm.dto.chat.ConversationPreviewDto;
import org.landm.dto.chat.MessageDto;
import org.landm.dto.chat.SendMessageRequestDto;
import org.landm.entity.Ad;
import org.landm.entity.Conversation;
import org.landm.entity.Enums.MessageType;
import org.landm.entity.Message;
import org.landm.entity.User;
import org.landm.mapper.ChatMapper;
import org.landm.repository.AdRepository;
import org.landm.repository.ConversationRepository;
import org.landm.repository.MessageRepository;
import org.landm.repository.UserRepository;
import org.landm.service.ChatService;
import org.landm.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final ChatMapper chatMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    public ChatServiceImpl(ConversationRepository conversationRepository, MessageRepository messageRepository, UserRepository userRepository, AdRepository adRepository, ChatMapper chatMapper, SimpMessagingTemplate messagingTemplate, NotificationService notificationService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.chatMapper = chatMapper;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
    }


    @Override
    public MessageDto sendMessage(SendMessageRequestDto request, Long senderId) {
        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Nije pronadjen user"));
        User receiver = userRepository.findById(request.getReceiverId()).orElseThrow(() -> new RuntimeException("Nije pronadjen user"));

        Ad ad = adRepository.findById(request.getAdId()).orElseThrow(() -> new RuntimeException("Nije pronadjen ad"));
        if (senderId.equals(request.getReceiverId())) {
            throw new IllegalArgumentException("Ne možete poslati poruku samom sebi.");
        }
        Optional<Conversation> existingConv = conversationRepository.findExistingConversation(ad.getId(), senderId, receiver.getId());
        Conversation conv;
        if(existingConv.isPresent()){
            conv = existingConv.get();
        }else {
            conv = new Conversation(ad, sender, receiver);
            conv = conversationRepository.save(conv);
        }
        Message message = new Message(conv, sender, request.getContent());
        messageRepository.save(message);

        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);

        notificationService.sendPushNotification(
            receiver.getId(),
            "New message from " + sender.getFirstname(),
            request.getContent().length() > 80
                ? request.getContent().substring(0, 80) + "..."
                : request.getContent()
        );

        return chatMapper.toMessageDto(message);
    }

    @Override
    public Page<ConversationPreviewDto> getMyConversations(Long myUserId, Pageable pageable) {

        User me =userRepository.findById(myUserId).orElseThrow(() -> new RuntimeException("Nije pronadjen user"));
        Page<Conversation> conversations = conversationRepository.findAllByUserId(myUserId, pageable);
        return conversations.map(conversation -> chatMapper.toDto(conversation, myUserId));
    }

    @Override
    @Transactional
    public void sendSystemMessage(Long adId, Long lesseeId, Long lessorId, String content, Long actorId) {
        Optional<Conversation> convOpt = conversationRepository.findExistingConversation(adId, lesseeId, lessorId);
        if (convOpt.isEmpty()) return;

        User actor = userRepository.findById(actorId).orElse(null);
        if (actor == null) return;

        Conversation conv = convOpt.get();
        Message systemMsg = new Message(conv, actor, content);
        systemMsg.setMessageType(MessageType.SYSTEM);
        messageRepository.save(systemMsg);

        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);

        MessageDto dto = chatMapper.toMessageDto(systemMsg);
        messagingTemplate.convertAndSendToUser(String.valueOf(lesseeId), "/queue/messages", dto);
        messagingTemplate.convertAndSendToUser(String.valueOf(lessorId), "/queue/messages", dto);
    }

    @Override
    @Transactional
    public void sendContractRequestMessage(org.landm.entity.RentalContract contract) {
        Long adId = contract.getAd().getId();
        Long lesseeId = contract.getLessee().getId();
        Long lessorId = contract.getAd().getOwner().getId();

        Ad ad = contract.getAd();
        User lessee = contract.getLessee();
        User lessor = userRepository.findById(lessorId).orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Conversation> convOpt = conversationRepository.findExistingConversation(adId, lesseeId, lessorId);
        Conversation conv;
        if (convOpt.isPresent()) {
            conv = convOpt.get();
        } else {
            conv = new Conversation(ad, lessee, lessor);
            conv = conversationRepository.save(conv);
        }

        Message msg = new Message(conv, lessee, "Poslat je zahtev za iznajmljivanje.");
        msg.setMessageType(MessageType.CONTRACT_REQUEST);
        msg.setRelatedContractId(contract.getId());
        messageRepository.save(msg);

        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);

        MessageDto dto = chatMapper.toMessageDto(msg);
        dto.setContractAdTitle(ad.getTitle());
        dto.setContractStartDate(contract.getStartDate().toString());
        dto.setContractEndDate(contract.getEndDate().toString());
        dto.setContractTotalPrice(contract.getAgreedPrice()
                .multiply(java.math.BigDecimal.valueOf(contract.getAmount())));
        dto.setContractCurrency(contract.getCurrency().name());

        messagingTemplate.convertAndSendToUser(String.valueOf(lesseeId), "/queue/messages", dto);
        messagingTemplate.convertAndSendToUser(String.valueOf(lessorId), "/queue/messages", dto);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return messageRepository.countUnreadForUser(userId);
    }

    @Override
    @Transactional
    public Page<MessageDto> getMessagesForConversation(Long conversationId, Long myUserId, Pageable pageable) {

        Conversation conversation =  conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if(!conversation.getParticipantOne().getId().equals(myUserId) && !conversation.getParticipantTwo().getId().equals(myUserId)){
            throw new RuntimeException("Nemate pristup ovoj konverzacviji");
        }

        messageRepository.markMessageAsRead(conversationId, myUserId);
        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);
        return messages.map(chatMapper::toMessageDto);
    }

}
