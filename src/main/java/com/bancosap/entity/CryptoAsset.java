package com.bancosap.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "crypto_assets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"wallet_id", "symbol"})
})
public class CryptoAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private CryptoWallet wallet;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "average_purchase_price", precision = 28, scale = 8)
    private BigDecimal averagePurchasePrice = BigDecimal.ZERO;

    public CryptoAsset() {}

    public CryptoAsset(CryptoWallet wallet, String symbol, String name, BigDecimal balance) {
        this.wallet = wallet;
        this.symbol = symbol;
        this.name = name;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.averagePurchasePrice = BigDecimal.ZERO;
    }

    public CryptoAsset(CryptoWallet wallet, String symbol, String name, BigDecimal balance, BigDecimal averagePurchasePrice) {
        this.wallet = wallet;
        this.symbol = symbol;
        this.name = name;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.averagePurchasePrice = averagePurchasePrice != null ? averagePurchasePrice : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CryptoWallet getWallet() { return wallet; }
    public void setWallet(CryptoWallet wallet) { this.wallet = wallet; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getAveragePurchasePrice() { return averagePurchasePrice; }
    public void setAveragePurchasePrice(BigDecimal averagePurchasePrice) { this.averagePurchasePrice = averagePurchasePrice; }
}
