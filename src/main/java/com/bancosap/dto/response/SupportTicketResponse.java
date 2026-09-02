package com.bancosap.dto.response;

import com.bancosap.enums.TicketStatus;
import java.time.LocalDateTime;
import java.util.List;

public class SupportTicketResponse {
    private Long id;
    private String protocol;
    private String subject;
    private String category;
    private TicketStatus status;
    private String userEmail;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SupportMessageResponse> messages;

    public SupportTicketResponse() {}

    public SupportTicketResponse(Long id, String protocol, String subject, String category,
                                 TicketStatus status, String userEmail, String userName,
                                 LocalDateTime createdAt, LocalDateTime updatedAt,
                                 List<SupportMessageResponse> messages) {
        this.id = id;
        this.protocol = protocol;
        this.subject = subject;
        this.category = category;
        this.status = status;
        this.userEmail = userEmail;
        this.userName = userName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messages = messages;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<SupportMessageResponse> getMessages() { return messages; }
    public void setMessages(List<SupportMessageResponse> messages) { this.messages = messages; }
}
