package org.landm.dto.chatbot;

public class ChatbotResponseDto {
    private String reply;

    public ChatbotResponseDto(String reply) {
        this.reply = reply;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}