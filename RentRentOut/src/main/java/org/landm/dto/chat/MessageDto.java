package org.landm.dto.chat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MessageDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private boolean isRead;
    private String messageType;
    private Long relatedContractId;
    private LocalDateTime createdAt;

    // Populated only for CONTRACT_REQUEST messages
    private String contractAdTitle;
    private String contractStartDate;
    private String contractEndDate;
    private BigDecimal contractTotalPrice;
    private String contractCurrency;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Long getRelatedContractId() {
        return relatedContractId;
    }

    public void setRelatedContractId(Long relatedContractId) {
        this.relatedContractId = relatedContractId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getContractAdTitle() { return contractAdTitle; }
    public void setContractAdTitle(String contractAdTitle) { this.contractAdTitle = contractAdTitle; }

    public String getContractStartDate() { return contractStartDate; }
    public void setContractStartDate(String contractStartDate) { this.contractStartDate = contractStartDate; }

    public String getContractEndDate() { return contractEndDate; }
    public void setContractEndDate(String contractEndDate) { this.contractEndDate = contractEndDate; }

    public BigDecimal getContractTotalPrice() { return contractTotalPrice; }
    public void setContractTotalPrice(BigDecimal contractTotalPrice) { this.contractTotalPrice = contractTotalPrice; }

    public String getContractCurrency() { return contractCurrency; }
    public void setContractCurrency(String contractCurrency) { this.contractCurrency = contractCurrency; }
}
