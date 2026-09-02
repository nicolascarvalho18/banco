package com.bancosap.controller;

import com.bancosap.dto.request.DepositRequest;
import com.bancosap.dto.response.AccountSummaryResponse;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.entity.Transaction;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Contas Bancárias & Saldos", description = "Gestão de saldo, extratos, reservas e depósitos simulados")
@SecurityRequirement(name = "BearerAuth")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Obter resumo da conta", description = "Retorna agência, conta, saldos (corrente e reserva), limites e totais de receita/despesa do mês.")
    public ResponseEntity<AccountSummaryResponse> getAccountSummary(@AuthenticationPrincipal UserPrincipal user) {
        AccountSummaryResponse summary = accountService.getAccountSummary(user.getId());
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/deposit")
    @Operation(summary = "Realizar depósito simulado", description = "Adiciona saldo demonstrativo à conta para testes de transferência e compras.")
    public ResponseEntity<Map<String, Object>> deposit(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody DepositRequest request,
            HttpServletRequest httpRequest) {
        Transaction tx = accountService.deposit(user.getId(), request, httpRequest);
        return ResponseEntity.ok(Map.of(
                "message", "Depósito realizado com sucesso.",
                "amount", tx.getAmount(),
                "authenticationCode", tx.getAuthenticationCode()
        ));
    }

    @PostMapping("/savings/apply")
    @Operation(summary = "Transferir para reserva financeira", description = "Move saldo da conta corrente para a poupança/reserva.")
    public ResponseEntity<Map<String, String>> applySavings(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, BigDecimal> body,
            HttpServletRequest httpRequest) {
        BigDecimal amount = body.get("amount");
        accountService.transferToSavings(user.getId(), amount, httpRequest);
        return ResponseEntity.ok(Map.of("message", "Valor aplicado na reserva com sucesso."));
    }

    @PostMapping("/savings/withdraw")
    @Operation(summary = "Resgatar da reserva financeira", description = "Move saldo da poupança/reserva para a conta corrente.")
    public ResponseEntity<Map<String, String>> withdrawSavings(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, BigDecimal> body,
            HttpServletRequest httpRequest) {
        BigDecimal amount = body.get("amount");
        accountService.withdrawFromSavings(user.getId(), amount, httpRequest);
        return ResponseEntity.ok(Map.of("message", "Valor resgatado para a conta corrente com sucesso."));
    }
}
