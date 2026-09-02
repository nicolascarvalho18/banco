package com.bancosap.controller;

import com.bancosap.dto.request.CryptoTradeRequest;
import com.bancosap.dto.request.CryptoTransferRequest;
import com.bancosap.dto.response.CryptoPortfolioResponse;
import com.bancosap.dto.response.CryptoQuoteResponse;
import com.bancosap.dto.response.CryptoTransactionResponse;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.CryptoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crypto")
@Tag(name = "Criptoativos Demonstrativos", description = "Simulação de carteira de criptomoedas, cotações em tempo real, compra/venda e envio P2P")
@SecurityRequirement(name = "BearerAuth")
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/quotes")
    @Operation(summary = "Obter cotações de mercado simuladas", description = "Retorna preços, variações em 24h e volumes de BTC, ETH, SOL, USDT e ADA.")
    public ResponseEntity<List<CryptoQuoteResponse>> getQuotes() {
        return ResponseEntity.ok(cryptoService.getMarketQuotes());
    }

    @GetMapping("/portfolio")
    @Operation(summary = "Consultar carteira do usuário", description = "Retorna saldo total convertido em BRL, saldos individuais por criptoativo e histórico de transações.")
    public ResponseEntity<CryptoPortfolioResponse> getPortfolio(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(cryptoService.getPortfolio(user.getId()));
    }

    @PostMapping("/trade")
    @Operation(summary = "Comprar ou vender criptoativos", description = "Executa a conversão demonstrativa entre Reais (BRL) e a criptomoeda selecionada.")
    public ResponseEntity<CryptoTransactionResponse> trade(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CryptoTradeRequest request,
            HttpServletRequest httpRequest) {
        CryptoTransactionResponse response = cryptoService.trade(user.getId(), request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer-p2p")
    @Operation(summary = "Transferência P2P entre correntistas", description = "Transfere criptoativos para outro usuário da plataforma através do endereço da carteira SAP.")
    public ResponseEntity<CryptoTransactionResponse> transferP2P(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CryptoTransferRequest request,
            HttpServletRequest httpRequest) {
        CryptoTransactionResponse response = cryptoService.transferP2P(user.getId(), request, httpRequest);
        return ResponseEntity.ok(response);
    }
}
