package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.chatbot.ChatbotRequestDto;
import org.landm.dto.chatbot.ChatbotResponseDto;
import org.landm.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support")
public class ChatbotController {
    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public ResponseEntity<ChatbotResponseDto> askBot(@Valid @RequestBody ChatbotRequestDto requestDto,
                                                     Authentication auth) {
        Long userId = resolveUserId(auth);
        String aiReply = chatbotService.askQuestion(requestDto.getMessage(), userId);
        return ResponseEntity.ok(new ChatbotResponseDto(aiReply));
    }

    private Long resolveUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
