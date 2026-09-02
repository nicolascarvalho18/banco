package com.bancosap.entity;

import com.bancosap.enums.CryptoOperationType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crypto_transactions")
public class CryptoTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tx_hash", nullable = false, unique = true, length = 66)
    private String txHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id")
    private CryptoWallet sourceWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_wallet_id")
    private CryptoWallet destinationWallet;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal quantity;

    @Column(name = "unit_price_brl", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPriceBrl;

    @Column(name = "total_brl", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalBrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 30)
    private CryptoOperationType operationType;

    @Column(nullable = false, length = 20)
    private String status = "CONCLUIDA";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CryptoTransaction() {}

    public CryptoTransaction(String txHash, CryptoWallet sourceWallet, CryptoWallet destinationWallet,
                             String symbol, BigDecimal quantity, BigDecimal unitPriceBrl,
                             BigDecimal totalBrl, CryptoOperationType operationType) {
        this.txHash = txHash;
        this.sourceWallet = sourceWallet;
        this.destinationWallet = destinationWallet;
        this.symbol = symbol;
        this.quantity = quantity;
        this.unitPriceBrl = unitPriceBrl;
        this.totalBrl = totalBrl;
        this.operationType = operationType;
        this.status = "CONCLUIDA";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }

    public CryptoWallet getSourceWallet() { return sourceWallet; }
    public void setSourceWallet(CryptoWallet sourceWallet) { this.sourceWallet = sourceWallet; }

    public CryptoWallet getDestinationWallet() { return destinationWallet; }
    public void setDestinationWallet(CryptoWallet destinationWallet) { this.destinationWallet = destinationWallet; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPriceBrl() { return unitPriceBrl; }
    public void setUnitPriceBrl(BigDecimal unitPriceBrl) { this.unitPriceBrl = unitPriceBrl; }

    public BigDecimal getTotalBrl() { return totalBrl; }
    public void setTotalBrl(BigDecimal totalBrl) { this.totalBrl = totalBrl; }

    public CryptoOperationType getOperationType() { return operationType; }
    public void setOperationType(CryptoOperationType operationType) { this.operationType = operationType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
