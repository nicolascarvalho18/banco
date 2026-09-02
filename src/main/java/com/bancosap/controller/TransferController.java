package com.bancosap.controller;

import com.bancosap.dto.request.TransferRequest;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transferências entre Contas", description = "Transferências bancárias internas e entre correntistas do Banco SAP")
@SecurityRequirement(name = "BearerAuth")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Operation(summary = "Executar transferência bancária", description = "Transfere valor atômico para outro correntista via conta, CPF ou e-mail com confirmação de PIN.")
    public ResponseEntity<TransactionResponse> executeTransfer(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest) {
        TransactionResponse response = transferService.executeTransfer(user.getId(), request, httpRequest);
        return ResponseEntity.ok(response);
    }
}
