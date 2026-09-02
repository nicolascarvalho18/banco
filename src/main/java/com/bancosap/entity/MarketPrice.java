package com.bancosap.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_prices")
public class MarketPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(name = "price_brl", nullable = false, precision = 19, scale = 4)
    private BigDecimal priceBrl;

    @Column(name = "price_usd", nullable = false, precision = 19, scale = 4)
    private BigDecimal priceUsd;

    @Column(name = "change_1h", precision = 8, scale = 2)
    private BigDecimal change1h = BigDecimal.ZERO;

    @Column(name = "change_24h", precision = 8, scale = 2)
    private BigDecimal change24h = BigDecimal.ZERO;

    @Column(name = "change_7d", precision = 8, scale = 2)
    private BigDecimal change7d = BigDecimal.ZERO;

    @Column(name = "volume_24h_brl", precision = 24, scale = 2)
    private BigDecimal volume24hBrl = BigDecimal.ZERO;

    @Column(name = "market_cap_brl", precision = 24, scale = 2)
    private BigDecimal marketCapBrl = BigDecimal.ZERO;

    @Column(name = "high_24h_brl", precision = 19, scale = 4)
    private BigDecimal high24hBrl = BigDecimal.ZERO;

    @Column(name = "low_24h_brl", precision = 19, scale = 4)
    private BigDecimal low24hBrl = BigDecimal.ZERO;

    @Column(name = "circulating_supply", precision = 24, scale = 4)
    private BigDecimal circulatingSupply = BigDecimal.ZERO;

    @Column(length = 30)
    private String category = "LAYER1";

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    public MarketPrice() {}

    public MarketPrice(String symbol, String name, BigDecimal priceBrl, BigDecimal priceUsd, BigDecimal change24h, BigDecimal volume24hBrl, BigDecimal marketCapBrl, String category, String iconUrl) {
        this.symbol = symbol;
        this.name = name;
        this.priceBrl = priceBrl;
        this.priceUsd = priceUsd;
        this.change24h = change24h;
        this.volume24hBrl = volume24hBrl;
        this.marketCapBrl = marketCapBrl;
        this.category = category;
        this.iconUrl = iconUrl;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPriceBrl() { return priceBrl; }
    public void setPriceBrl(BigDecimal priceBrl) { this.priceBrl = priceBrl; }

    public BigDecimal getPriceUsd() { return priceUsd; }
    public void setPriceUsd(BigDecimal priceUsd) { this.priceUsd = priceUsd; }

    public BigDecimal getChange1h() { return change1h; }
    public void setChange1h(BigDecimal change1h) { this.change1h = change1h; }

    public BigDecimal getChange24h() { return change24h; }
    public void setChange24h(BigDecimal change24h) { this.change24h = change24h; }

    public BigDecimal getChange7d() { return change7d; }
    public void setChange7d(BigDecimal change7d) { this.change7d = change7d; }

    public BigDecimal getVolume24hBrl() { return volume24hBrl; }
    public void setVolume24hBrl(BigDecimal volume24hBrl) { this.volume24hBrl = volume24hBrl; }

    public BigDecimal getMarketCapBrl() { return marketCapBrl; }
    public void setMarketCapBrl(BigDecimal marketCapBrl) { this.marketCapBrl = marketCapBrl; }

    public BigDecimal getHigh24hBrl() { return high24hBrl; }
    public void setHigh24hBrl(BigDecimal high24hBrl) { this.high24hBrl = high24hBrl; }

    public BigDecimal getLow24hBrl() { return low24hBrl; }
    public void setLow24hBrl(BigDecimal low24hBrl) { this.low24hBrl = low24hBrl; }

    public BigDecimal getCirculatingSupply() { return circulatingSupply; }
    public void setCirculatingSupply(BigDecimal circulatingSupply) { this.circulatingSupply = circulatingSupply; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}
