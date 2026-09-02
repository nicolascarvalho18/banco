package com.bancosap.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LedgerEntryResponse {
    private String entryCode;
    private String transactionReference;
    private String entryType;
    private String assetSymbol;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private LocalDateTime createdAt;

    public LedgerEntryResponse() {}

    public LedgerEntryResponse(String entryCode, String transactionReference, String entryType, String assetSymbol, BigDecimal amount, BigDecimal balanceAfter, String description, LocalDateTime createdAt) {
        this.entryCode = entryCode;
        this.transactionReference = transactionReference;
        this.entryType = entryType;
        this.assetSymbol = assetSymbol;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getEntryCode() { return entryCode; }
    public String getTransactionReference() { return transactionReference; }
    public String getEntryType() { return entryType; }
    public String getAssetSymbol() { return assetSymbol; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
