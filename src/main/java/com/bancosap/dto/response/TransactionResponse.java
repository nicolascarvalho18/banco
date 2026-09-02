package com.bancosap.dto.response;

import com.bancosap.enums.TransactionCategory;
import com.bancosap.enums.TransactionStatus;
import com.bancosap.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private String authenticationCode;
    private TransactionType type;
    private String typeDescription;
    private BigDecimal amount;
    private BigDecimal fee;
    private TransactionCategory category;
    private String description;
    private String destinationName;
    private String destinationDocument;
    private String destinationBank;
    private String sourceName;
    private String sourceAccountNumber;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private boolean incoming;

    public TransactionResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAuthenticationCode() { return authenticationCode; }
    public void setAuthenticationCode(String authenticationCode) { this.authenticationCode = authenticationCode; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getTypeDescription() { return typeDescription; }
    public void setTypeDescription(String typeDescription) { this.typeDescription = typeDescription; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public TransactionCategory getCategory() { return category; }
    public void setCategory(TransactionCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }

    public String getDestinationDocument() { return destinationDocument; }
    public void setDestinationDocument(String destinationDocument) { this.destinationDocument = destinationDocument; }

    public String getDestinationBank() { return destinationBank; }
    public void setDestinationBank(String destinationBank) { this.destinationBank = destinationBank; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isIncoming() { return incoming; }
    public void setIncoming(boolean incoming) { this.incoming = incoming; }
}
