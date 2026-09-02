package com.bancosap.service;

import com.bancosap.dto.response.PortfolioSummaryResponse;
import com.bancosap.dto.response.PortfolioSummaryResponse.CryptoAssetSummaryResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.CryptoAsset;
import com.bancosap.entity.CryptoWallet;
import com.bancosap.entity.MarketPrice;
import com.bancosap.market.MarketDataService;
import com.bancosap.repository.AccountRepository;
import com.bancosap.repository.CryptoAssetRepository;
import com.bancosap.repository.CryptoWalletRepository;
import com.bancosap.repository.MarketPriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CryptoPortfolioService {

    private final AccountRepository accountRepository;
    private final CryptoWalletRepository walletRepository;
    private final CryptoAssetRepository assetRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final MarketDataService marketDataService;

    public CryptoPortfolioService(AccountRepository accountRepository,
                                  CryptoWalletRepository walletRepository,
                                  CryptoAssetRepository assetRepository,
                                  MarketPriceRepository marketPriceRepository,
                                  MarketDataService marketDataService) {
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;
        this.assetRepository = assetRepository;
        this.marketPriceRepository = marketPriceRepository;
        this.marketDataService = marketDataService;
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getPortfolioSummary(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseGet(() -> new Account(null, "33458-1", BigDecimal.ZERO));

        BigDecimal brlBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;

        CryptoWallet wallet = walletRepository.findByUserId(userId).orElse(null);
        String walletAddress = wallet != null ? wallet.getWalletAddress() : "0xSAF000000000000000000000000000000000000";

        List<CryptoAsset> assets = wallet != null
                ? assetRepository.findByWalletId(wallet.getId())
                : List.of();

        Map<String, MarketPrice> marketMap = marketPriceRepository.findAll().stream()
                .collect(Collectors.toMap(m -> m.getSymbol().toUpperCase(), m -> m, (a, b) -> a));

        BigDecimal cryptoTotalBrl = BigDecimal.ZERO;
        List<CryptoAssetSummaryResponse> assetDtos = new ArrayList<>();

        for (CryptoAsset asset : assets) {
            if (asset.getBalance() != null && asset.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                String sym = asset.getSymbol().toUpperCase();
                MarketPrice mp = marketMap.get(sym);
                BigDecimal priceBrl = mp != null ? mp.getPriceBrl() : BigDecimal.ZERO;
                String iconUrl = mp != null ? mp.getIconUrl() : null;
                String name = mp != null ? mp.getName() : sym;

                BigDecimal totalValueBrl = asset.getBalance().multiply(priceBrl).setScale(2, RoundingMode.HALF_DOWN);
                cryptoTotalBrl = cryptoTotalBrl.add(totalValueBrl);

                // Lucro / Prejuízo Real Baseado em Custo Médio
                BigDecimal avgPrice = (asset.getAveragePurchasePrice() != null && asset.getAveragePurchasePrice().compareTo(BigDecimal.ZERO) > 0)
                        ? asset.getAveragePurchasePrice()
                        : (priceBrl.compareTo(BigDecimal.ZERO) > 0 ? priceBrl.multiply(new BigDecimal("0.94")).setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO);

                BigDecimal costBasis = asset.getBalance().multiply(avgPrice).setScale(2, RoundingMode.HALF_DOWN);
                BigDecimal profitLossBrl = totalValueBrl.subtract(costBasis);
                BigDecimal profitLossPercent = (costBasis.compareTo(BigDecimal.ZERO) > 0)
                        ? profitLossBrl.divide(costBasis, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                assetDtos.add(new CryptoAssetSummaryResponse(
                        sym,
                        name,
                        iconUrl,
                        asset.getBalance(),
                        priceBrl,
                        totalValueBrl,
                        BigDecimal.ZERO, // Alocação calculada a seguir
                        avgPrice,
                        profitLossBrl,
                        profitLossPercent
                ));
            }
        }

        BigDecimal totalNetWorthBrl = brlBalance.add(cryptoTotalBrl);

        // Calcular percentuais de alocação de ativos
        List<CryptoAssetSummaryResponse> finalizedAssets = new ArrayList<>();
        for (CryptoAssetSummaryResponse dto : assetDtos) {
            BigDecimal alloc = totalNetWorthBrl.compareTo(BigDecimal.ZERO) > 0
                    ? dto.getTotalValueBrl().divide(totalNetWorthBrl, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            finalizedAssets.add(new CryptoAssetSummaryResponse(
                    dto.getSymbol(),
                    dto.getName(),
                    dto.getIconUrl(),
                    dto.getBalance(),
                    dto.getCurrentPriceBrl(),
                    dto.getTotalValueBrl(),
                    alloc,
                    dto.getAveragePurchasePrice(),
                    dto.getProfitLossBrl(),
                    dto.getProfitLossPercent()
            ));
        }

        // Variação e PnL consolidado em 24h
        BigDecimal pnl24hBrl = BigDecimal.ZERO;
        for (CryptoAssetSummaryResponse dto : finalizedAssets) {
            MarketPrice mp = marketMap.get(dto.getSymbol());
            if (mp != null && mp.getChange24h() != null) {
                BigDecimal assetChange = dto.getTotalValueBrl().multiply(mp.getChange24h()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                pnl24hBrl = pnl24hBrl.add(assetChange);
            }
        }

        BigDecimal pnl24hPercent = totalNetWorthBrl.compareTo(BigDecimal.ZERO) > 0
                ? pnl24hBrl.divide(totalNetWorthBrl, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new PortfolioSummaryResponse(
                totalNetWorthBrl,
                brlBalance,
                cryptoTotalBrl,
                pnl24hBrl,
                pnl24hPercent,
                walletAddress,
                finalizedAssets
        );
    }
}
