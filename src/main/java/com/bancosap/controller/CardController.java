package com.bancosap.controller;

import com.bancosap.dto.request.CreateVirtualCardRequest;
import com.bancosap.dto.request.SimulatePurchaseRequest;
import com.bancosap.dto.request.ToggleCardStatusRequest;
import com.bancosap.dto.request.UpdateCardLimitRequest;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.dto.response.VirtualCardResponse;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@Tag(name = "Cartões Físicos & Virtuais", description = "Gestão de cartões demonstrativos, ajuste de limites, bloqueios e simulação de compras")
@SecurityRequirement(name = "BearerAuth")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    @Operation(summary = "Listar cartões da conta", description = "Retorna todos os cartões (físicos e virtuais) vinculados à conta do usuário.")
    public ResponseEntity<List<VirtualCardResponse>> getMyCards(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(cardService.getAccountCards(user.getId()));
    }

    @PostMapping("/virtual")
    @Operation(summary = "Criar novo cartão virtual", description = "Gera um novo cartão virtual temporário ou recorrente com limite personalizável.")
    public ResponseEntity<VirtualCardResponse> createVirtualCard(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CreateVirtualCardRequest request,
            HttpServletRequest httpRequest) {
        VirtualCardResponse response = cardService.createVirtualCard(user.getId(), request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/status")
    @Operation(summary = "Bloquear ou desbloquear cartão", description = "Altera o status operacional do cartão instantaneamente.")
    public ResponseEntity<VirtualCardResponse> toggleStatus(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ToggleCardStatusRequest request,
            HttpServletRequest httpRequest) {
        VirtualCardResponse response = cardService.toggleCardStatus(user.getId(), request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/limit")
    @Operation(summary = "Ajustar limite de gastos do cartão", description = "Modifica o limite disponível do cartão mediante validação de PIN.")
    public ResponseEntity<VirtualCardResponse> updateLimit(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody UpdateCardLimitRequest request,
            HttpServletRequest httpRequest) {
        VirtualCardResponse response = cardService.updateSpendingLimit(user.getId(), request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/simulate-purchase")
    @Operation(summary = "Simular compra na internet", description = "Testa o fluxo de compra online utilizando token do cartão, CVV e valor.")
    public ResponseEntity<TransactionResponse> simulatePurchase(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody SimulatePurchaseRequest request,
            HttpServletRequest httpRequest) {
        TransactionResponse response = cardService.simulatePurchase(user.getId(), request, httpRequest);
        return ResponseEntity.ok(response);
    }
}
