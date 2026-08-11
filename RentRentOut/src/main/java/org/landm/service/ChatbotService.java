package org.landm.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatbotService {

    String askQuestion(String userMessage, Long userId);

    SseEmitter streamQuestion(String userMessage, Long userId);
}
