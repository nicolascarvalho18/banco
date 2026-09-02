package com.bancosap.dto.response;

import com.bancosap.enums.CryptoOperationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CryptoTransactionResponse {
    private Long id;
    private String txHash;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal unitPriceBrl;
    private BigDecimal totalBrl;
    private CryptoOperationType operationType;
    private String sourceWalletAddress;
    private String destinationWalletAddress;
    private String status;
    private LocalDateTime createdAt;

    public CryptoTransactionResponse() {}

    public CryptoTransactionResponse(Long id, String txHash, String symbol, BigDecimal quantity,
                                   BigDecimal unitPriceBrl, BigDecimal totalBrl,
                                   CryptoOperationType operationType, String sourceWalletAddress,
                                   String destinationWalletAddress, String status, LocalDateTime createdAt) {
        this.id = id;
        this.txHash = txHash;
        this.symbol = symbol;
        this.quantity = quantity;
        this.unitPriceBrl = unitPriceBrl;
        this.totalBrl = totalBrl;
        this.operationType = operationType;
        this.sourceWalletAddress = sourceWalletAddress;
        this.destinationWalletAddress = destinationWalletAddress;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }

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

    public String getSourceWalletAddress() { return sourceWalletAddress; }
    public void setSourceWalletAddress(String sourceWalletAddress) { this.sourceWalletAddress = sourceWalletAddress; }

    public String getDestinationWalletAddress() { return destinationWalletAddress; }
    public void setDestinationWalletAddress(String destinationWalletAddress) { this.destinationWalletAddress = destinationWalletAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
