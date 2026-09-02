package com.bancosap.dto.response;

import com.bancosap.enums.CardStatus;
import com.bancosap.enums.CardType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VirtualCardResponse {
    private Long id;
    private String cardNumberMasked;
    private String cardNumberToken;
    private String holderName;
    private String expirationDate;
    private String cvvSimulated;
    private CardType cardType;
    private CardStatus status;
    private BigDecimal spendingLimit;
    private BigDecimal usedLimit;
    private BigDecimal availableLimit;
    private boolean temporary;
    private LocalDateTime createdAt;

    public VirtualCardResponse() {}

    public VirtualCardResponse(Long id, String cardNumberMasked, String cardNumberToken,
                               String holderName, String expirationDate, String cvvSimulated,
                               CardType cardType, CardStatus status, BigDecimal spendingLimit,
                               BigDecimal usedLimit, boolean temporary, LocalDateTime createdAt) {
        this.id = id;
        this.cardNumberMasked = cardNumberMasked;
        this.cardNumberToken = cardNumberToken;
        this.holderName = holderName;
        this.expirationDate = expirationDate;
        this.cvvSimulated = cvvSimulated;
        this.cardType = cardType;
        this.status = status;
        this.spendingLimit = spendingLimit;
        this.usedLimit = usedLimit;
        this.availableLimit = spendingLimit.subtract(usedLimit);
        this.temporary = temporary;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public BigDecimal getAvailableLimit() { return availableLimit; }
    public void setAvailableLimit(BigDecimal availableLimit) { this.availableLimit = availableLimit; }

    public boolean isTemporary() { return temporary; }
    public void setTemporary(boolean temporary) { this.temporary = temporary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
