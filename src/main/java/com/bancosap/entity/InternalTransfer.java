package com.bancosap.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "internal_transfers")
public class InternalTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "authentication_code", nullable = false, unique = true, length = 64)
    private String authenticationCode = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false, length = 20)
    private String symbol; // BRL, BTC, ETH, etc.

    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal amount;

    @Column(name = "amount_brl_equivalent", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountBrlEquivalent;

    @Column(name = "fee_brl", precision = 19, scale = 2)
    private BigDecimal feeBrl = BigDecimal.ZERO;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 20)
    private String status = "CONCLUIDA";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public InternalTransfer() {}

    public InternalTransfer(User sender, User recipient, String symbol, BigDecimal amount, BigDecimal amountBrlEquivalent, BigDecimal feeBrl, String description) {
        this.sender = sender;
        this.recipient = recipient;
        this.symbol = symbol;
        this.amount = amount;
        this.amountBrlEquivalent = amountBrlEquivalent;
        this.feeBrl = feeBrl;
        this.description = description;
        this.authenticationCode = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAuthenticationCode() { return authenticationCode; }
    public void setAuthenticationCode(String authenticationCode) { this.authenticationCode = authenticationCode; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getAmountBrlEquivalent() { return amountBrlEquivalent; }
    public void setAmountBrlEquivalent(BigDecimal amountBrlEquivalent) { this.amountBrlEquivalent = amountBrlEquivalent; }

    public BigDecimal getFeeBrl() { return feeBrl; }
    public void setFeeBrl(BigDecimal feeBrl) { this.feeBrl = feeBrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
