package com.bancosap.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioSummaryResponse {
    private BigDecimal totalNetWorthBrl;
    private BigDecimal brlBalance;
    private BigDecimal cryptoTotalBrl;
    private BigDecimal pnl24hBrl;
    private BigDecimal pnl24hPercent;
    private String walletAddress;
    private List<CryptoAssetSummaryResponse> assets;

    public PortfolioSummaryResponse() {}

    public PortfolioSummaryResponse(BigDecimal totalNetWorthBrl, BigDecimal brlBalance, BigDecimal cryptoTotalBrl, BigDecimal pnl24hBrl, BigDecimal pnl24hPercent, String walletAddress, List<CryptoAssetSummaryResponse> assets) {
        this.totalNetWorthBrl = totalNetWorthBrl;
        this.brlBalance = brlBalance;
        this.cryptoTotalBrl = cryptoTotalBrl;
        this.pnl24hBrl = pnl24hBrl;
        this.pnl24hPercent = pnl24hPercent;
        this.walletAddress = walletAddress;
        this.assets = assets;
    }

    public BigDecimal getTotalNetWorthBrl() { return totalNetWorthBrl; }
    public BigDecimal getBrlBalance() { return brlBalance; }
    public BigDecimal getCryptoTotalBrl() { return cryptoTotalBrl; }
    public BigDecimal getPnl24hBrl() { return pnl24hBrl; }
    public BigDecimal getPnl24hPercent() { return pnl24hPercent; }
    public String getWalletAddress() { return walletAddress; }
    public List<CryptoAssetSummaryResponse> getAssets() { return assets; }

    public static class CryptoAssetSummaryResponse {
        private String symbol;
        private String name;
        private String iconUrl;
        private BigDecimal balance;
        private BigDecimal currentPriceBrl;
        private BigDecimal totalValueBrl;
        private BigDecimal allocationPercent;
        private BigDecimal averagePurchasePrice;
        private BigDecimal profitLossBrl;
        private BigDecimal profitLossPercent;

        public CryptoAssetSummaryResponse() {}

        public CryptoAssetSummaryResponse(String symbol, String name, String iconUrl, BigDecimal balance, BigDecimal currentPriceBrl, BigDecimal totalValueBrl, BigDecimal allocationPercent, BigDecimal averagePurchasePrice, BigDecimal profitLossBrl, BigDecimal profitLossPercent) {
            this.symbol = symbol;
            this.name = name;
            this.iconUrl = iconUrl;
            this.balance = balance;
            this.currentPriceBrl = currentPriceBrl;
            this.totalValueBrl = totalValueBrl;
            this.allocationPercent = allocationPercent;
            this.averagePurchasePrice = averagePurchasePrice;
            this.profitLossBrl = profitLossBrl;
            this.profitLossPercent = profitLossPercent;
        }

        public String getSymbol() { return symbol; }
        public String getName() { return name; }
        public String getIconUrl() { return iconUrl; }
        public BigDecimal getBalance() { return balance; }
        public BigDecimal getCurrentPriceBrl() { return currentPriceBrl; }
        public BigDecimal getTotalValueBrl() { return totalValueBrl; }
        public BigDecimal getAllocationPercent() { return allocationPercent; }
        public BigDecimal getAveragePurchasePrice() { return averagePurchasePrice; }
        public BigDecimal getProfitLossBrl() { return profitLossBrl; }
        public BigDecimal getProfitLossPercent() { return profitLossPercent; }
    }
}
