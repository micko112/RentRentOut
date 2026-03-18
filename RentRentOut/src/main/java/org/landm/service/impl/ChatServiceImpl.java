package org.landm.service.impl;


import jakarta.transaction.Transactional;
import org.landm.dto.chat.ConversationPreviewDto;
import org.landm.dto.chat.MessageDto;
import org.landm.dto.chat.SendMessageRequestDto;
import org.landm.entity.Ad;
import org.landm.entity.Conversation;
import org.landm.entity.Message;
import org.landm.entity.User;
import org.landm.mapper.ChatMapper;
import org.landm.repository.AdRepository;
import org.landm.repository.ConversationRepository;
import org.landm.repository.MessageRepository;
import org.landm.repository.UserRepository;
import org.landm.service.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public ChatServiceImpl(ConversationRepository conversationRepository, MessageRepository messageRepository, UserRepository userRepository, AdRepository adRepository, ChatMapper chatMapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.chatMapper = chatMapper;
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
