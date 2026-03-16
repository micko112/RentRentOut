package org.landm.controller;

import org.landm.dto.chat.MessageDto;
import org.landm.dto.chat.SendMessageRequestDto;
import org.landm.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller

public class ChatWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public ChatWsController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequestDto request, Principal principal){
        Long senderId = Long.parseLong(principal.getName());

        if (principal == null) {
            System.out.println("⛔ GREŠKA U CHATU: Principal je NULL! Korisnik nije prepoznat u cevi!");
            return;
        }
        MessageDto savedMessage = chatService.sendMessage(request, senderId);
        System.out.println("✅ STIGLA PORUKA! Šalje je korisnik ID: " + principal.getName() + " Sadržaj: " + request.getContent());
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
