package com.bancosap.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InternalTransferResponse {
    private String authenticationCode;
    private String senderUsername;
    private String senderName;
    private String recipientUsername;
    private String recipientName;
    private String symbol;
    private BigDecimal amount;
    private BigDecimal amountBrlEquivalent;
    private BigDecimal feeBrl;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    public InternalTransferResponse() {}

    public InternalTransferResponse(String authenticationCode, String senderUsername, String senderName, String recipientUsername, String recipientName, String symbol, BigDecimal amount, BigDecimal amountBrlEquivalent, BigDecimal feeBrl, String description, String status, LocalDateTime createdAt) {
        this.authenticationCode = authenticationCode;
        this.senderUsername = senderUsername;
        this.senderName = senderName;
        this.recipientUsername = recipientUsername;
        this.recipientName = recipientName;
        this.symbol = symbol;
        this.amount = amount;
        this.amountBrlEquivalent = amountBrlEquivalent;
        this.feeBrl = feeBrl;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getAuthenticationCode() { return authenticationCode; }
    public String getSenderUsername() { return senderUsername; }
    public String getSenderName() { return senderName; }
    public String getRecipientUsername() { return recipientUsername; }
    public String getRecipientName() { return recipientName; }
    public String getSymbol() { return symbol; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getAmountBrlEquivalent() { return amountBrlEquivalent; }
    public BigDecimal getFeeBrl() { return feeBrl; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
