package com.bancosap.dto.response;

import java.math.BigDecimal;

public class CryptoAssetResponse {
    private String symbol;
    private String name;
    private BigDecimal balance;
    private BigDecimal unitPriceBrl;
    private BigDecimal totalValueBrl;
    private BigDecimal change24hPercent;

    public CryptoAssetResponse() {}

    public CryptoAssetResponse(String symbol, String name, BigDecimal balance,
                               BigDecimal unitPriceBrl, BigDecimal totalValueBrl, BigDecimal change24hPercent) {
        this.symbol = symbol;
        this.name = name;
        this.balance = balance;
        this.unitPriceBrl = unitPriceBrl;
        this.totalValueBrl = totalValueBrl;
        this.change24hPercent = change24hPercent;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getUnitPriceBrl() { return unitPriceBrl; }
    public void setUnitPriceBrl(BigDecimal unitPriceBrl) { this.unitPriceBrl = unitPriceBrl; }

    public BigDecimal getTotalValueBrl() { return totalValueBrl; }
    public void setTotalValueBrl(BigDecimal totalValueBrl) { this.totalValueBrl = totalValueBrl; }

    public BigDecimal getChange24hPercent() { return change24hPercent; }
    public void setChange24hPercent(BigDecimal change24hPercent) { this.change24hPercent = change24hPercent; }
}
