package com.bancosap.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class CryptoPortfolioResponse {
    private String walletAddress;
    private BigDecimal totalValueBrl;
    private List<CryptoAssetResponse> assets;
    private List<CryptoTransactionResponse> recentTransactions;
    private String disclaimer = "AVISO LEGAL: Os valores, cotações e ativos apresentados são estritamente demonstrativos e operam em ambiente de simulação financeira. Não representam investimentos reais nem garantem rendimentos.";

    public CryptoPortfolioResponse() {}

    public CryptoPortfolioResponse(String walletAddress, BigDecimal totalValueBrl,
                                   List<CryptoAssetResponse> assets, List<CryptoTransactionResponse> recentTransactions) {
        this.walletAddress = walletAddress;
        this.totalValueBrl = totalValueBrl;
        this.assets = assets;
        this.recentTransactions = recentTransactions;
    }

    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }

    public BigDecimal getTotalValueBrl() { return totalValueBrl; }
    public void setTotalValueBrl(BigDecimal totalValueBrl) { this.totalValueBrl = totalValueBrl; }

    public List<CryptoAssetResponse> getAssets() { return assets; }
    public void setAssets(List<CryptoAssetResponse> assets) { this.assets = assets; }

    public List<CryptoTransactionResponse> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<CryptoTransactionResponse> recentTransactions) { this.recentTransactions = recentTransactions; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
