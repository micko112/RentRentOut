package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.chat.ConversationPreviewDto;
import org.landm.dto.chat.MessageDto;
import org.landm.dto.chat.SendMessageRequestDto;
import org.landm.service.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ResponseEntity<MessageDto> sendMessage(@Valid @RequestBody SendMessageRequestDto request, Authentication auth) {
        Long myId = Long.parseLong(auth.getName());
        return new ResponseEntity<>(chatService.sendMessage(request, myId), HttpStatus.CREATED);
    }

    @GetMapping("/conversations")
    public ResponseEntity<Page<ConversationPreviewDto>> getMyConversations(Authentication auth, Pageable pageable) {
        Long myId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(chatService.getMyConversations(myId, pageable));
    }

    @GetMapping("/conversations/{convId}/messages")
    public ResponseEntity<Page<MessageDto>> getMessages(
            @PathVariable Long convId,
            Authentication auth,
            Pageable pageable) {
        Long myId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(chatService.getMessagesForConversation(convId, myId, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication auth) {
        Long myId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(chatService.getUnreadCount(myId));
    }
}
