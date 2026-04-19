package org.landm.dto.chatbot;

import jakarta.validation.constraints.NotBlank;

public class ChatbotRequestDto {
    @NotBlank
    private String message;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}