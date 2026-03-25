package org.landm.controller;

import org.landm.dto.chat.MessageDto;
import org.landm.dto.chat.SendMessageRequestDto;
import org.landm.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller

public class ChatWsController {

    private static final Logger log = LoggerFactory.getLogger(ChatWsController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public ChatWsController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequestDto request, Principal principal){
        if (principal == null) {
            log.warn("GREŠKA U CHATU: Principal je NULL!");
            return;
        }
        Long senderId = Long.parseLong(principal.getName());
        MessageDto savedMessage = chatService.sendMessage(request, senderId);
        log.debug("Poruka primljena od korisnika ID: {}", senderId);
        messagingTemplate.convertAndSendToUser(
                String.valueOf(request.getReceiverId()),
                "/queue/messages",
                savedMessage
        );

        messagingTemplate.convertAndSendToUser(
                String.valueOf(senderId),
                "/queue/messages",
                savedMessage
        );

    }
}
