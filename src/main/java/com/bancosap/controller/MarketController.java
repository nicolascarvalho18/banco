package com.bancosap.controller;

import com.bancosap.dto.response.MarketHistoryResponse;
import com.bancosap.dto.response.MarketTickerResponse;
import com.bancosap.market.MarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
@Tag(name = "Mercado & Cotações Reais", description = "Endpoints de cotações em tempo real e séries históricas de preços")
public class MarketController {

    private final MarketDataService marketDataService;

    public MarketController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/tickers")
    @Operation(summary = "Listar cotações de todas as criptomoedas suportadas")
    public ResponseEntity<List<MarketTickerResponse>> getAllTickers() {
        return ResponseEntity.ok(marketDataService.getAllTickers());
    }

    @GetMapping("/tickers/{symbol}")
    @Operation(summary = "Obter cotação detalhada de uma criptomoeda")
    public ResponseEntity<MarketTickerResponse> getTicker(@PathVariable String symbol) {
        return ResponseEntity.ok(marketDataService.getTicker(symbol));
    }

    @GetMapping("/history/{symbol}")
    @Operation(summary = "Obter histórico de preços para gráficos interativos (1H, 24H, 7D, 30D, 1Y, ALL)")
    public ResponseEntity<MarketHistoryResponse> getHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "24H") String timeframe) {
        return ResponseEntity.ok(marketDataService.getPriceHistory(symbol, timeframe));
    }
}
