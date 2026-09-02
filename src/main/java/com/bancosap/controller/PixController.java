package com.bancosap.controller;

import com.bancosap.dto.request.PixKeyCreateRequest;
import com.bancosap.dto.request.PixSendRequest;
import com.bancosap.dto.response.PixKeyResponse;
import com.bancosap.dto.response.PixQrCodeResponse;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.PixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pix")
@Tag(name = "PIX Demonstrativo", description = "Gestão de chaves PIX, envio instantâneo e QR Codes demonstrativos")
@SecurityRequirement(name = "BearerAuth")
public class PixController {

    private final PixService pixService;

    public PixController(PixService pixService) {
        this.pixService = pixService;
    }

    @GetMapping("/keys")
    @Operation(summary = "Listar chaves PIX cadastradas", description = "Retorna todas as chaves PIX vinculadas à conta do usuário.")
    public ResponseEntity<List<PixKeyResponse>> getMyKeys(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(pixService.getUserPixKeys(user.getId()));
    }

    @PostMapping("/keys")
    @Operation(summary = "Cadastrar nova chave PIX", description = "Registra chave do tipo CPF, Email, Telefone ou Aleatória.")
    public ResponseEntity<PixKeyResponse> createKey(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody PixKeyCreateRequest request,
            HttpServletRequest httpRequest) {
        PixKeyResponse response = pixService.createPixKey(user.getId(), request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/keys/{keyId}")
    @Operation(summary = "Excluir chave PIX", description = "Remove chave PIX associada à conta.")
    public ResponseEntity<Map<String, String>> deleteKey(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long keyId,
            HttpServletRequest httpRequest) {
        pixService.deletePixKey(user.getId(), keyId, httpRequest);
        return ResponseEntity.ok(Map.of("message", "Chave PIX removida com sucesso."));
    }

    @PostMapping("/send")
    @Operation(summary = "Enviar PIX", description = "Executa envio instantâneo via chave PIX ou payload Copia e Cola com confirmação por PIN.")
    public ResponseEntity<TransactionResponse> sendPix(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody PixSendRequest request,
            HttpServletRequest httpRequest) {
        TransactionResponse response = pixService.sendPix(user.getId(), request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/qr-code")
    @Operation(summary = "Gerar QR Code PIX", description = "Gera payload Copia e Cola e imagem base64 de QR Code para recebimento.")
    public ResponseEntity<PixQrCodeResponse> generateQrCode(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String key) {
        return ResponseEntity.ok(pixService.generateQrCode(user.getId(), amount, key));
    }
}
