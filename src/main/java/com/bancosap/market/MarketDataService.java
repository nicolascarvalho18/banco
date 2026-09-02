package com.bancosap.market;

import com.bancosap.dto.response.MarketHistoryResponse;
import com.bancosap.dto.response.MarketTickerResponse;
import com.bancosap.entity.MarketPrice;
import com.bancosap.repository.MarketPriceRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);
    private final MarketPriceRepository marketPriceRepository;
    private final RestTemplate restTemplate;

    // Cache em memória de alta velocidade
    private final Map<String, MarketPrice> memoryCache = new ConcurrentHashMap<>();
    private LocalDateTime lastSuccessfulSync = LocalDateTime.now();
    private String connectionStatus = "ONLINE"; // ONLINE, DEGRADED, OFFLINE

    private static final Map<String, String> COINGECKO_ID_MAP = Map.ofEntries(
            Map.entry("BTC", "bitcoin"),
            Map.entry("ETH", "ethereum"),
            Map.entry("SOL", "solana"),
            Map.entry("BNB", "binancecoin"),
            Map.entry("XRP", "ripple"),
            Map.entry("ADA", "cardano"),
            Map.entry("DOGE", "dogecoin"),
            Map.entry("LINK", "chainlink"),
            Map.entry("AVAX", "avalanche-2"),
            Map.entry("MATIC", "matic-network"),
            Map.entry("LTC", "litecoin"),
            Map.entry("DOT", "polkadot"),
            Map.entry("USDT", "tether"),
            Map.entry("USDC", "usd-coin")
    );

    private static final List<MarketPrice> INITIAL_ASSETS = List.of(
            new MarketPrice("BTC", "Bitcoin", new BigDecimal("362450.00"), new BigDecimal("64500.00"), new BigDecimal("2.85"), new BigDecimal("18500000000.00"), new BigDecimal("7150000000000.00"), "LAYER1", "https://cryptologos.cc/logos/bitcoin-btc-logo.svg"),
            new MarketPrice("ETH", "Ethereum", new BigDecimal("18240.00"), new BigDecimal("3250.00"), new BigDecimal("3.40"), new BigDecimal("9400000000.00"), new BigDecimal("2200000000000.00"), "LAYER1", "https://cryptologos.cc/logos/ethereum-eth-logo.svg"),
            new MarketPrice("SOL", "Solana", new BigDecimal("845.50"), new BigDecimal("150.50"), new BigDecimal("5.75"), new BigDecimal("4200000000.00"), new BigDecimal("390000000000.00"), "LAYER1", "https://cryptologos.cc/logos/solana-sol-logo.svg"),
            new MarketPrice("BNB", "BNB", new BigDecimal("3250.00"), new BigDecimal("580.00"), new BigDecimal("1.10"), new BigDecimal("1200000000.00"), new BigDecimal("495000000000.00"), "LAYER1", "https://cryptologos.cc/logos/bnb-bnb-logo.svg"),
            new MarketPrice("XRP", "XRP", new BigDecimal("3.35"), new BigDecimal("0.60"), new BigDecimal("1.80"), new BigDecimal("1500000000.00"), new BigDecimal("187000000000.00"), "PAYMENT", "https://cryptologos.cc/logos/xrp-xrp-logo.svg"),
            new MarketPrice("ADA", "Cardano", new BigDecimal("2.15"), new BigDecimal("0.38"), new BigDecimal("2.10"), new BigDecimal("480000000.00"), new BigDecimal("76000000000.00"), "LAYER1", "https://cryptologos.cc/logos/cardano-ada-logo.svg"),
            new MarketPrice("DOGE", "Dogecoin", new BigDecimal("0.68"), new BigDecimal("0.12"), new BigDecimal("4.30"), new BigDecimal("890000000.00"), new BigDecimal("98000000000.00"), "MEME", "https://cryptologos.cc/logos/dogecoin-doge-logo.svg"),
            new MarketPrice("LINK", "Chainlink", new BigDecimal("68.40"), new BigDecimal("12.20"), new BigDecimal("3.15"), new BigDecimal("350000000.00"), new BigDecimal("41000000000.00"), "ORACLE", "https://cryptologos.cc/logos/chainlink-link-logo.svg"),
            new MarketPrice("AVAX", "Avalanche", new BigDecimal("145.80"), new BigDecimal("26.00"), new BigDecimal("4.80"), new BigDecimal("410000000.00"), new BigDecimal("58000000000.00"), "LAYER1", "https://cryptologos.cc/logos/avalanche-avax-logo.svg"),
            new MarketPrice("MATIC", "Polygon", new BigDecimal("2.45"), new BigDecimal("0.44"), new BigDecimal("1.90"), new BigDecimal("290000000.00"), new BigDecimal("24000000000.00"), "LAYER2", "https://cryptologos.cc/logos/polygon-matic-logo.svg"),
            new MarketPrice("LTC", "Litecoin", new BigDecimal("410.00"), new BigDecimal("73.00"), new BigDecimal("1.40"), new BigDecimal("310000000.00"), new BigDecimal("30500000000.00"), "PAYMENT", "https://cryptologos.cc/logos/litecoin-ltc-logo.svg"),
            new MarketPrice("DOT", "Polkadot", new BigDecimal("26.50"), new BigDecimal("4.70"), new BigDecimal("2.30"), new BigDecimal("210000000.00"), new BigDecimal("37000000000.00"), "LAYER0", "https://cryptologos.cc/logos/polkadot-new-dot-logo.svg"),
            new MarketPrice("USDT", "Tether USD", new BigDecimal("5.62"), new BigDecimal("1.00"), new BigDecimal("0.05"), new BigDecimal("52000000000.00"), new BigDecimal("640000000000.00"), "STABLECOIN", "https://cryptologos.cc/logos/tether-usdt-logo.svg"),
            new MarketPrice("USDC", "USD Coin", new BigDecimal("5.62"), new BigDecimal("1.00"), new BigDecimal("0.02"), new BigDecimal("12000000000.00"), new BigDecimal("190000000000.00"), "STABLECOIN", "https://cryptologos.cc/logos/usd-coin-usdc-logo.svg")
    );

    public MarketDataService(MarketPriceRepository marketPriceRepository, RestTemplateBuilder restTemplateBuilder) {
        this.marketPriceRepository = marketPriceRepository;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(4))
                .setReadTimeout(Duration.ofSeconds(4))
                .build();
    }

    @PostConstruct
    public void initMarketAssets() {
        if (marketPriceRepository.count() == 0) {
            log.info("Inicializando cotações dos 14 criptoativos suportados...");
            for (MarketPrice asset : INITIAL_ASSETS) {
                marketPriceRepository.save(asset);
                memoryCache.put(asset.getSymbol().toUpperCase(), asset);
            }
        } else {
            marketPriceRepository.findAll().forEach(a -> memoryCache.put(a.getSymbol().toUpperCase(), a));
        }

        // Tentar buscar cotações reais imediatamente
        new Thread(this::fetchRealMarketQuotes).start();
    }

    /**
     * Atualização agendada periódica (a cada 30 segundos)
     */
    @Scheduled(fixedRate = 30000, initialDelay = 10000)
    public void scheduledPriceUpdate() {
        fetchRealMarketQuotes();
    }

    public synchronized void fetchRealMarketQuotes() {
        try {
            String ids = String.join(",", COINGECKO_ID_MAP.values());
            String url = "https://api.coingecko.com/api/v3/simple/price" +
                    "?ids=" + ids +
                    "&vs_currencies=brl,usd" +
                    "&include_24hr_vol=true" +
                    "&include_24hr_change=true" +
                    "&include_market_cap=true";

            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> response = restTemplate.getForObject(url, Map.class);

            if (response != null && !response.isEmpty()) {
                for (Map.Entry<String, String> entry : COINGECKO_ID_MAP.entrySet()) {
                    String symbol = entry.getKey();
                    String geckoId = entry.getValue();
                    Map<String, Object> data = response.get(geckoId);

                    if (data != null) {
                        updateAssetQuote(symbol, data);
                    }
                }
                this.lastSuccessfulSync = LocalDateTime.now();
                this.connectionStatus = "ONLINE";
                log.info("Cotações reais de mercado sincronizadas com sucesso da CoinGecko.");
            }
        } catch (Exception e) {
            log.warn("Falha temporária ao sincronizar cotações da API pública (usando cache local): {}", e.getMessage());
            this.connectionStatus = "DEGRADED";
        }
    }

    private void updateAssetQuote(String symbol, Map<String, Object> data) {
        marketPriceRepository.findBySymbolIgnoreCase(symbol).ifPresent(asset -> {
            Number brlPrice = (Number) data.get("brl");
            Number usdPrice = (Number) data.get("usd");
            Number change24h = (Number) data.get("brl_24h_change");
            Number vol24h = (Number) data.get("brl_24h_vol");
            Number marketCap = (Number) data.get("brl_market_cap");

            if (brlPrice != null) asset.setPriceBrl(BigDecimal.valueOf(brlPrice.doubleValue()));
            if (usdPrice != null) asset.setPriceUsd(BigDecimal.valueOf(usdPrice.doubleValue()));
            if (change24h != null) asset.setChange24h(BigDecimal.valueOf(change24h.doubleValue()).setScale(2, RoundingMode.HALF_UP));
            if (vol24h != null) asset.setVolume24hBrl(BigDecimal.valueOf(vol24h.doubleValue()));
            if (marketCap != null) asset.setMarketCapBrl(BigDecimal.valueOf(marketCap.doubleValue()));

            asset.setLastUpdatedAt(LocalDateTime.now());
            marketPriceRepository.save(asset);
            memoryCache.put(symbol, asset);
        });
    }

    public List<MarketTickerResponse> getAllTickers() {
        List<MarketPrice> list = marketPriceRepository.findAllByOrderByMarketCapBrlDesc();
        if (list.isEmpty()) {
            list = new ArrayList<>(memoryCache.values());
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public MarketTickerResponse getTicker(String symbol) {
        MarketPrice asset = marketPriceRepository.findBySymbolIgnoreCase(symbol)
                .or(() -> Optional.ofNullable(memoryCache.get(symbol.toUpperCase())))
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado: " + symbol));
        return toDto(asset);
    }

    public BigDecimal getPriceInBrl(String symbol) {
        if ("BRL".equalsIgnoreCase(symbol)) return BigDecimal.ONE;
        return marketPriceRepository.findBySymbolIgnoreCase(symbol)
                .map(MarketPrice::getPriceBrl)
                .or(() -> Optional.ofNullable(memoryCache.get(symbol.toUpperCase())).map(MarketPrice::getPriceBrl))
                .orElseThrow(() -> new IllegalArgumentException("Cotação indisponível para o ativo: " + symbol));
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public LocalDateTime getLastSuccessfulSync() {
        return lastSuccessfulSync;
    }

    /**
     * Gera série histórica de preços para gráficos em múltiplos períodos (1H, 24H, 7D, 30D, 1Y, ALL)
     */
    public MarketHistoryResponse getPriceHistory(String symbol, String timeframe) {
        MarketPrice asset = marketPriceRepository.findBySymbolIgnoreCase(symbol)
                .or(() -> Optional.ofNullable(memoryCache.get(symbol.toUpperCase())))
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado: " + symbol));

        BigDecimal currentPrice = asset.getPriceBrl();
        int pointsCount = switch (timeframe.toUpperCase()) {
            case "1H" -> 12;
            case "24H" -> 24;
            case "7D" -> 28;
            case "30D" -> 30;
            case "1Y" -> 52;
            default -> 60;
        };

        List<String> labels = new ArrayList<>();
        List<BigDecimal> prices = new ArrayList<>();

        double base = currentPrice.doubleValue();
        double variance = switch (timeframe.toUpperCase()) {
            case "1H" -> 0.005;
            case "24H" -> 0.025;
            case "7D" -> 0.08;
            case "30D" -> 0.18;
            default -> 0.40;
        };

        Random random = new Random(symbol.hashCode() + timeframe.hashCode());
        double walker = base * (1.0 - (asset.getChange24h().doubleValue() / 100.0));

        for (int i = 0; i < pointsCount - 1; i++) {
            double step = (random.nextDouble() - 0.48) * (base * variance / (pointsCount / 2.0));
            walker += step;
            if (walker <= 0) walker = base * 0.1;
            prices.add(BigDecimal.valueOf(walker).setScale(2, RoundingMode.HALF_UP));
            labels.add("T-" + (pointsCount - i));
        }

        // Último ponto é o preço real atual
        prices.add(currentPrice.setScale(2, RoundingMode.HALF_UP));
        labels.add("Agora");

        return new MarketHistoryResponse(symbol, asset.getName(), currentPrice, timeframe, labels, prices);
    }

    private MarketTickerResponse toDto(MarketPrice m) {
        return new MarketTickerResponse(
                m.getSymbol(),
                m.getName(),
                m.getPriceBrl(),
                m.getPriceUsd(),
                m.getChange1h(),
                m.getChange24h(),
                m.getChange7d(),
                m.getVolume24hBrl(),
                m.getMarketCapBrl(),
                m.getHigh24hBrl(),
                m.getLow24hBrl(),
                m.getCirculatingSupply(),
                m.getCategory(),
                m.getIconUrl(),
                m.getLastUpdatedAt(),
                this.connectionStatus
        );
    }
}
