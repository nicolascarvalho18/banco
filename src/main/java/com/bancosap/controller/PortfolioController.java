package com.bancosap.controller;

import com.bancosap.dto.response.PortfolioSummaryResponse;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.CryptoPortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portfolio")
@Tag(name = "Carteira & Patrimônio Cripto", description = "Consolidação de patrimônio, cotações em tempo real e lucros/prejuízos")
public class PortfolioController {

    private final CryptoPortfolioService portfolioService;

    public PortfolioController(CryptoPortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    @Operation(summary = "Obter resumo consolidado do patrimônio, saldo em reais e alocação de criptoativos")
    public ResponseEntity<PortfolioSummaryResponse> getPortfolio(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(portfolioService.getPortfolioSummary(currentUser.getId()));
    }
}
