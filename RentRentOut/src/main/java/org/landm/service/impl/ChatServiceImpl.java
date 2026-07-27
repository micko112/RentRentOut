package org.landm.service.impl;


import org.landm.dto.chat.ConversationPreviewDto;
import org.landm.dto.chat.MessageDto;
import org.landm.dto.chat.SendMessageRequestDto;
import org.landm.entity.Ad;
import org.landm.entity.Conversation;
import org.landm.entity.Enums.MessageType;
import org.landm.entity.Message;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.landm.mapper.ChatMapper;
import org.landm.repository.AdRepository;
import org.landm.repository.ConversationRepository;
import org.landm.repository.MessageRepository;
import org.landm.repository.UserRepository;
import org.landm.exception.UserNotFoundException;
import org.landm.service.ChatService;
import org.landm.service.NotificationService;
import org.landm.util.HtmlSanitizer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final MessageSource messageSource;

    public ChatServiceImpl(ConversationRepository conversationRepository, MessageRepository messageRepository, UserRepository userRepository, AdRepository adRepository, ChatMapper chatMapper, SimpMessagingTemplate messagingTemplate, NotificationService notificationService, MessageSource messageSource) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.chatMapper = chatMapper;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, org.springframework.context.i18n.LocaleContextHolder.getLocale());
    }


    @Override
    @Transactional
    public MessageDto sendMessage(SendMessageRequestDto request, Long senderId) {
        if (request.getReceiverId() == null || request.getAdId() == null) {
            throw new IllegalArgumentException(msg("error.chat.receiver_and_ad_required"));
        }

        MessageType type;
        try {
            type = request.getMessageType() == null || request.getMessageType().isBlank()
                    ? MessageType.REGULAR
                    : MessageType.valueOf(request.getMessageType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(msg("error.chat.unknown_message_type"));
        }
        if (type != MessageType.REGULAR && type != MessageType.IMAGE && type != MessageType.LOCATION) {
            throw new IllegalArgumentException(msg("error.chat.disallowed_message_type"));
        }

        if (type == MessageType.REGULAR) {
            if (request.getContent() == null || request.getContent().isBlank()) {
                throw new IllegalArgumentException(msg("error.chat.message_empty"));
            }
            if (request.getContent().length() > 5000) {
                throw new IllegalArgumentException(msg("error.chat.message_too_long"));
            }
        } else if (type == MessageType.IMAGE) {
            if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
                throw new IllegalArgumentException(msg("error.chat.image_url_required"));
            }
            if (!isAllowedImageUrl(request.getImageUrl())) {
                throw new IllegalArgumentException(msg("error.chat.disallowed_image_source"));
            }
        } else { // LOCATION
            if (request.getLocationLat() == null || request.getLocationLng() == null) {
                throw new IllegalArgumentException(msg("error.chat.coordinates_required"));
            }
        }

        User sender = userRepository.findById(senderId).orElseThrow(() -> new UserNotFoundException("User not found"));
        User receiver = userRepository.findById(request.getReceiverId()).orElseThrow(() -> new UserNotFoundException("User not found"));

        Ad ad = adRepository.findById(request.getAdId()).orElseThrow(() -> new IllegalArgumentException(msg("error.ad.not_found")));
        if (senderId.equals(request.getReceiverId())) {
            throw new IllegalArgumentException(msg("error.chat.cannot_message_self"));
        }
        Optional<Conversation> existingConv = conversationRepository.findExistingConversation(ad.getId(), senderId, receiver.getId());
        Conversation conv;
        if(existingConv.isPresent()){
            conv = existingConv.get();
        }else {
            conv = new Conversation(ad, sender, receiver);
            conv = conversationRepository.save(conv);
        }

        Message message = new Message();
        message.setConversation(conv);
        message.setSender(sender);
        message.setMessageType(type);

        String pushPreview;
        if (type == MessageType.REGULAR) {
            String sanitizedContent = HtmlSanitizer.sanitize(request.getContent());
            message.setContent(sanitizedContent);
            pushPreview = sanitizedContent.length() > 80
                    ? sanitizedContent.substring(0, 80) + "..."
                    : sanitizedContent;
        } else if (type == MessageType.IMAGE) {
            message.setImageUrl(request.getImageUrl());
            pushPreview = msg("chat.push.image_sent");
        } else {
            message.setLocationLat(request.getLocationLat());
            message.setLocationLng(request.getLocationLng());
            if (request.getLocationLabel() != null && !request.getLocationLabel().isBlank()) {
                message.setLocationLabel(HtmlSanitizer.sanitize(request.getLocationLabel()));
            }
            pushPreview = msg("chat.push.location_sent");
        }

        messageRepository.save(message);

        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);

        notificationService.sendPushNotification(
            receiver.getId(),
            msg("chat.push.new_message_from", sender.getFirstname()),
            pushPreview
        );

        return chatMapper.toMessageDto(message);
    }

    private boolean isAllowedImageUrl(String url) {
        String trimmed = url.trim().toLowerCase();
        return trimmed.startsWith("https://res.cloudinary.com/");
    }

    @Override
    public Page<ConversationPreviewDto> getMyConversations(Long myUserId, Pageable pageable) {
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
    public void sendContractRequestMessage(RentalContract contract) {
        Long adId = contract.getAd().getId();
        Long lesseeId = contract.getLessee().getId();
        Long lessorId = contract.getAd().getOwner().getId();

        Ad ad = contract.getAd();
        User lessee = contract.getLessee();
        User lessor = userRepository.findById(lessorId).orElseThrow(() -> new UserNotFoundException("User not found"));

        Optional<Conversation> convOpt = conversationRepository.findExistingConversation(adId, lesseeId, lessorId);
        Conversation conv;
        if (convOpt.isPresent()) {
            conv = convOpt.get();
        } else {
            conv = new Conversation(ad, lessee, lessor);
            conv = conversationRepository.save(conv);
        }

        Message message = new Message(conv, lessee, msg("chat.system.contract_request_sent"));
        message.setMessageType(MessageType.CONTRACT_REQUEST);
        message.setRelatedContractId(contract.getId());
        messageRepository.save(message);

        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);

        MessageDto dto = chatMapper.toMessageDto(message);
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

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException(msg("error.chat.conversation_not_found")));

        if(!conversation.getParticipantOne().getId().equals(myUserId) && !conversation.getParticipantTwo().getId().equals(myUserId)){
            throw new AccessDeniedException(msg("error.chat.no_conversation_access"));
        }

        messageRepository.markMessageAsRead(conversationId, myUserId);
        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);
        return messages.map(chatMapper::toMessageDto);
    }

}
