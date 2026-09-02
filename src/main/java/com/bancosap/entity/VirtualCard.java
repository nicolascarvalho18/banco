package com.bancosap.entity;

import com.bancosap.enums.CardStatus;
import com.bancosap.enums.CardType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "virtual_cards")
public class VirtualCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "card_number_masked", nullable = false, length = 20)
    private String cardNumberMasked;

    @Column(name = "card_number_token", nullable = false, length = 64)
    private String cardNumberToken;

    @Column(name = "holder_name", nullable = false, length = 120)
    private String holderName;

    @Column(name = "expiration_date", nullable = false, length = 7)
    private String expirationDate;

    @Column(name = "cvv_simulated", nullable = false, length = 4)
    private String cvvSimulated;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 20)
    private CardType cardType = CardType.VIRTUAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status = CardStatus.ATIVO;

    @Column(name = "spending_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal spendingLimit = new BigDecimal("2000.00");

    @Column(name = "used_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal usedLimit = BigDecimal.ZERO;

    @Column(name = "is_temporary", nullable = false)
    private boolean temporary = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public VirtualCard() {}

    public VirtualCard(Account account, String cardNumberMasked, String cardNumberToken,
                       String holderName, String expirationDate, String cvvSimulated,
                       CardType cardType, BigDecimal spendingLimit, boolean temporary) {
        this.account = account;
        this.cardNumberMasked = cardNumberMasked;
        this.cardNumberToken = cardNumberToken;
        this.holderName = holderName;
        this.expirationDate = expirationDate;
        this.cvvSimulated = cvvSimulated;
        this.cardType = cardType;
        this.status = CardStatus.ATIVO;
        this.spendingLimit = spendingLimit;
        this.usedLimit = BigDecimal.ZERO;
        this.temporary = temporary;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public String getCardNumberMasked() { return cardNumberMasked; }
    public void setCardNumberMasked(String cardNumberMasked) { this.cardNumberMasked = cardNumberMasked; }

    public String getCardNumberToken() { return cardNumberToken; }
    public void setCardNumberToken(String cardNumberToken) { this.cardNumberToken = cardNumberToken; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getCvvSimulated() { return cvvSimulated; }
    public void setCvvSimulated(String cvvSimulated) { this.cvvSimulated = cvvSimulated; }

    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }

    public CardStatus getStatus() { return status; }
    public void setStatus(CardStatus status) { this.status = status; }

    public BigDecimal getSpendingLimit() { return spendingLimit; }
    public void setSpendingLimit(BigDecimal spendingLimit) { this.spendingLimit = spendingLimit; }

    public BigDecimal getUsedLimit() { return usedLimit; }
    public void setUsedLimit(BigDecimal usedLimit) { this.usedLimit = usedLimit; }

    public boolean isTemporary() { return temporary; }
    public void setTemporary(boolean temporary) { this.temporary = temporary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
