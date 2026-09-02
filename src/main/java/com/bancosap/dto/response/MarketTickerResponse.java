package com.bancosap.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketTickerResponse {
    private String symbol;
    private String name;
    private BigDecimal priceBrl;
    private BigDecimal priceUsd;
    private BigDecimal change1h;
    private BigDecimal change24h;
    private BigDecimal change7d;
    private BigDecimal volume24hBrl;
    private BigDecimal marketCapBrl;
    private BigDecimal high24hBrl;
    private BigDecimal low24hBrl;
    private BigDecimal circulatingSupply;
    private String category;
    private String iconUrl;
    private LocalDateTime lastUpdatedAt;
    private String connectionStatus;

    public MarketTickerResponse() {}

    public MarketTickerResponse(String symbol, String name, BigDecimal priceBrl, BigDecimal priceUsd, BigDecimal change1h, BigDecimal change24h, BigDecimal change7d, BigDecimal volume24hBrl, BigDecimal marketCapBrl, BigDecimal high24hBrl, BigDecimal low24hBrl, BigDecimal circulatingSupply, String category, String iconUrl, LocalDateTime lastUpdatedAt, String connectionStatus) {
        this.symbol = symbol;
        this.name = name;
        this.priceBrl = priceBrl;
        this.priceUsd = priceUsd;
        this.change1h = change1h;
        this.change24h = change24h;
        this.change7d = change7d;
        this.volume24hBrl = volume24hBrl;
        this.marketCapBrl = marketCapBrl;
        this.high24hBrl = high24hBrl;
        this.low24hBrl = low24hBrl;
        this.circulatingSupply = circulatingSupply;
        this.category = category;
        this.iconUrl = iconUrl;
        this.lastUpdatedAt = lastUpdatedAt;
        this.connectionStatus = connectionStatus;
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public BigDecimal getPriceBrl() { return priceBrl; }
    public BigDecimal getPriceUsd() { return priceUsd; }
    public BigDecimal getChange1h() { return change1h; }
    public BigDecimal getChange24h() { return change24h; }
    public BigDecimal getChange7d() { return change7d; }
    public BigDecimal getVolume24hBrl() { return volume24hBrl; }
    public BigDecimal getMarketCapBrl() { return marketCapBrl; }
    public BigDecimal getHigh24hBrl() { return high24hBrl; }
    public BigDecimal getLow24hBrl() { return low24hBrl; }
    public BigDecimal getCirculatingSupply() { return circulatingSupply; }
    public String getCategory() { return category; }
    public String getIconUrl() { return iconUrl; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public String getConnectionStatus() { return connectionStatus; }
}
