package com.bancosap.controller;

import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.enums.TransactionCategory;
import com.bancosap.enums.TransactionType;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.StatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/statement")
@Tag(name = "Extrato & Comprovantes", description = "Consulta de extrato consolidado, filtros por categoria/data/valor e exportação de relatórios")
@SecurityRequirement(name = "BearerAuth")
public class StatementController {

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    @GetMapping
    @Operation(summary = "Consultar extrato com filtros", description = "Lista transações filtrando por período, tipo, categoria de gastos e valores.")
    public ResponseEntity<Page<TransactionResponse>> getStatement(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) TransactionCategory category,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<TransactionResponse> page = statementService.getFilteredStatement(
                user.getId(), startDate, endDate, category, type, minAmount, maxAmount, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/receipt/{authCode}")
    @Operation(summary = "Obter comprovante por autenticação", description = "Retorna os detalhes completos do comprovante a partir do código de autenticação eletrônica.")
    public ResponseEntity<TransactionResponse> getReceipt(@PathVariable String authCode) {
        return ResponseEntity.ok(statementService.getReceiptByAuthCode(authCode));
    }

    @GetMapping("/export-csv")
    @Operation(summary = "Exportar extrato em CSV", description = "Gera download do arquivo CSV com as movimentações filtradas.")
    public ResponseEntity<byte[]> exportCsv(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        byte[] csvData = statementService.exportStatementCsv(user.getId(), startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"extrato_banco_sap.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
