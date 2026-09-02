package com.bancosap.controller;

import com.bancosap.dto.response.LedgerEntryResponse;
import com.bancosap.entity.LedgerEntry;
import com.bancosap.repository.LedgerEntryRepository;
import com.bancosap.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
@Tag(name = "Livro Razão & Auditoria de Lançamentos (Ledger)", description = "Registro contábil imutável de dupla entrada de todas as movimentações")
public class LedgerController {

    private final LedgerEntryRepository ledgerRepository;

    public LedgerController(LedgerEntryRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @GetMapping
    @Operation(summary = "Obter lançamentos do livro razão do usuário")
    public ResponseEntity<Page<LedgerEntryResponse>> getLedger(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) String asset,
            Pageable pageable) {

        Page<LedgerEntry> page = (asset != null && !asset.isBlank())
                ? ledgerRepository.findByUserIdAndAssetSymbolOrderByCreatedAtDesc(currentUser.getId(), asset.toUpperCase(), pageable)
                : ledgerRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);

        return ResponseEntity.ok(page.map(l -> new LedgerEntryResponse(
                l.getEntryCode(),
                l.getTransactionReference(),
                l.getEntryType(),
                l.getAssetSymbol(),
                l.getAmount(),
                l.getBalanceAfter(),
                l.getDescription(),
                l.getCreatedAt()
        )));
    }

    @GetMapping("/export-csv")
    @Operation(summary = "Exportar livro razão do usuário em arquivo CSV oficial")
    public ResponseEntity<byte[]> exportCsv(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<LedgerEntry> entries = ledgerRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), Pageable.unpaged()).getContent();

        StringBuilder csv = new StringBuilder();
        csv.append("Código Lançamento,Referência Transação,Tipo,Ativo,Quantidade,Saldo Resultante,Descrição,Data e Hora\n");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        for (LedgerEntry l : entries) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,\"%s\",\"%s\"\n",
                    l.getEntryCode(),
                    l.getTransactionReference(),
                    l.getEntryType(),
                    l.getAssetSymbol(),
                    l.getAmount(),
                    l.getBalanceAfter(),
                    l.getDescription().replace("\"", "\"\""),
                    l.getCreatedAt().format(fmt)));
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"livro_razao_banco_sap.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }
}
