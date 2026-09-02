package com.bancosap.dto.response;

import java.math.BigDecimal;

public class CryptoQuoteResponse {
    private String symbol;
    private String name;
    private BigDecimal priceBrl;
    private BigDecimal change24hPercent;
    private BigDecimal high24hBrl;
    private BigDecimal low24hBrl;
    private BigDecimal volume24hBrl;

    public CryptoQuoteResponse() {}

    public CryptoQuoteResponse(String symbol, String name, BigDecimal priceBrl,
                               BigDecimal change24hPercent, BigDecimal high24hBrl,
                               BigDecimal low24hBrl, BigDecimal volume24hBrl) {
        this.symbol = symbol;
        this.name = name;
        this.priceBrl = priceBrl;
        this.change24hPercent = change24hPercent;
        this.high24hBrl = high24hBrl;
        this.low24hBrl = low24hBrl;
        this.volume24hBrl = volume24hBrl;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPriceBrl() { return priceBrl; }
    public void setPriceBrl(BigDecimal priceBrl) { this.priceBrl = priceBrl; }

    public BigDecimal getChange24hPercent() { return change24hPercent; }
    public void setChange24hPercent(BigDecimal change24hPercent) { this.change24hPercent = change24hPercent; }

    public BigDecimal getHigh24hBrl() { return high24hBrl; }
    public void setHigh24hBrl(BigDecimal high24hBrl) { this.high24hBrl = high24hBrl; }

    public BigDecimal getLow24hBrl() { return low24hBrl; }
    public void setLow24hBrl(BigDecimal low24hBrl) { this.low24hBrl = low24hBrl; }

    public BigDecimal getVolume24hBrl() { return volume24hBrl; }
    public void setVolume24hBrl(BigDecimal volume24hBrl) { this.volume24hBrl = volume24hBrl; }
}
