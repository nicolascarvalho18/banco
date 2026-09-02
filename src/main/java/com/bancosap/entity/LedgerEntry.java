package com.bancosap.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_code", nullable = false, unique = true, length = 64)
    private String entryCode = UUID.randomUUID().toString();

    @Column(name = "transaction_reference", nullable = false, length = 64)
    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entry_type", nullable = false, length = 20)
    private String entryType; // DEBITO, CREDITO

    @Column(name = "asset_symbol", nullable = false, length = 20)
    private String assetSymbol; // BRL, BTC, ETH, etc.

    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 24, scale = 8)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public LedgerEntry() {}

    public LedgerEntry(String transactionReference, User user, String entryType, String assetSymbol, BigDecimal amount, BigDecimal balanceAfter, String description) {
        this.transactionReference = transactionReference;
        this.user = user;
        this.entryType = entryType;
        this.assetSymbol = assetSymbol;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.entryCode = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEntryCode() { return entryCode; }
    public void setEntryCode(String entryCode) { this.entryCode = entryCode; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }

    public String getAssetSymbol() { return assetSymbol; }
    public void setAssetSymbol(String assetSymbol) { this.assetSymbol = assetSymbol; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
