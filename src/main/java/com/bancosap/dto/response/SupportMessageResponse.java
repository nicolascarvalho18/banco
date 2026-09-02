package com.bancosap.dto.response;

import com.bancosap.enums.TicketSenderType;
import java.time.LocalDateTime;

public class SupportMessageResponse {
    private Long id;
    private TicketSenderType senderType;
    private String senderName;
    private String message;
    private LocalDateTime createdAt;

    public SupportMessageResponse() {}

    public SupportMessageResponse(Long id, TicketSenderType senderType, String senderName, String message, LocalDateTime createdAt) {
        this.id = id;
        this.senderType = senderType;
        this.senderName = senderName;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TicketSenderType getSenderType() { return senderType; }
    public void setSenderType(TicketSenderType senderType) { this.senderType = senderType; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
