package com.bancosap.controller;

import com.bancosap.dto.request.BillPaymentRequest;
import com.bancosap.dto.response.BillValidationResponse;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.BillPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bills")
@Tag(name = "Pagamentos & Boletos", description = "Validação e liquidação de boletos bancários simulados")
@SecurityRequirement(name = "BearerAuth")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar código de barras", description = "Decodifica a linha digitável/código de barras, identificando banco emissor, vencimento e valor.")
    public ResponseEntity<BillValidationResponse> validateBarcode(@RequestParam String barcode) {
        return ResponseEntity.ok(billPaymentService.validateBarcode(barcode));
    }

    @PostMapping("/pay")
    @Operation(summary = "Pagar boleto", description = "Efetua a liquidação do boleto bancário mediante confirmação do PIN de segurança.")
    public ResponseEntity<TransactionResponse> payBill(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody BillPaymentRequest request,
            HttpServletRequest httpRequest) {
        TransactionResponse response = billPaymentService.payBill(user.getId(), request, httpRequest);
        return ResponseEntity.ok(response);
    }
}
