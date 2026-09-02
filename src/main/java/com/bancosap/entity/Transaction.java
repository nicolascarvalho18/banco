package com.bancosap.entity;

import com.bancosap.enums.TransactionCategory;
import com.bancosap.enums.TransactionStatus;
import com.bancosap.enums.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "authentication_code", nullable = false, unique = true, length = 64)
    private String authenticationCode = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @Column(name = "destination_name", length = 120)
    private String destinationName;

    @Column(name = "destination_document", length = 20)
    private String destinationDocument;

    @Column(name = "destination_bank", length = 60)
    private String destinationBank;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 40)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionCategory category = TransactionCategory.OUTROS;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.CONCLUIDA;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Transaction() {}

    public Transaction(Account sourceAccount, Account destinationAccount, String destinationName,
                       String destinationDocument, String destinationBank, TransactionType transactionType,
                       BigDecimal amount, BigDecimal fee, TransactionCategory category, String description) {
        this.authenticationCode = UUID.randomUUID().toString().toUpperCase();
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.destinationName = destinationName;
        this.destinationDocument = destinationDocument;
        this.destinationBank = destinationBank != null ? destinationBank : "001 - Banco SAP";
        this.transactionType = transactionType;
        this.amount = amount;
        this.fee = fee != null ? fee : BigDecimal.ZERO;
        this.category = category != null ? category : TransactionCategory.OUTROS;
        this.description = description;
        this.status = TransactionStatus.CONCLUIDA;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAuthenticationCode() { return authenticationCode; }
    public void setAuthenticationCode(String authenticationCode) { this.authenticationCode = authenticationCode; }

    public Account getSourceAccount() { return sourceAccount; }
    public void setSourceAccount(Account sourceAccount) { this.sourceAccount = sourceAccount; }

    public Account getDestinationAccount() { return destinationAccount; }
    public void setDestinationAccount(Account destinationAccount) { this.destinationAccount = destinationAccount; }

    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }

    public String getDestinationDocument() { return destinationDocument; }
    public void setDestinationDocument(String destinationDocument) { this.destinationDocument = destinationDocument; }

    public String getDestinationBank() { return destinationBank; }
    public void setDestinationBank(String destinationBank) { this.destinationBank = destinationBank; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public TransactionCategory getCategory() { return category; }
    public void setCategory(TransactionCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
