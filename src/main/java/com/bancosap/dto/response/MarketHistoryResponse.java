package com.bancosap.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class MarketHistoryResponse {
    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private String timeframe;
    private List<String> labels;
    private List<BigDecimal> prices;

    public MarketHistoryResponse() {}

    public MarketHistoryResponse(String symbol, String name, BigDecimal currentPrice, String timeframe, List<String> labels, List<BigDecimal> prices) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
        this.timeframe = timeframe;
        this.labels = labels;
        this.prices = prices;
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public String getTimeframe() { return timeframe; }
    public List<String> getLabels() { return labels; }
    public List<BigDecimal> getPrices() { return prices; }
}
