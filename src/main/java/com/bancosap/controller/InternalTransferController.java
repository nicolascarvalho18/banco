package com.bancosap.controller;

import com.bancosap.dto.request.InternalTransferRequest;
import com.bancosap.dto.response.InternalTransferResponse;
import com.bancosap.entity.InternalTransfer;
import com.bancosap.repository.InternalTransferRepository;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.InternalTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers/internal")
@Tag(name = "Transferências Internas P2P", description = "Envio e recebimento de saldo e criptomoedas entre usuários da plataforma")
public class InternalTransferController {

    private final InternalTransferService transferService;
    private final InternalTransferRepository transferRepository;

    public InternalTransferController(InternalTransferService transferService, InternalTransferRepository transferRepository) {
        this.transferService = transferService;
        this.transferRepository = transferRepository;
    }

    @PostMapping
    @Operation(summary = "Realizar transferência P2P para outro usuário (por @username ou e-mail)")
    public ResponseEntity<InternalTransferResponse> transfer(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody InternalTransferRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(transferService.executeTransfer(currentUser.getId(), request, ip));
    }

    @GetMapping
    @Operation(summary = "Histórico de transferências P2P enviadas e recebidas")
    public ResponseEntity<Page<InternalTransferResponse>> getTransfers(
            @AuthenticationPrincipal UserPrincipal currentUser,
            Pageable pageable) {
        Page<InternalTransfer> page = transferRepository.findByUserId(currentUser.getId(), pageable);
        return ResponseEntity.ok(page.map(t -> new InternalTransferResponse(
                t.getAuthenticationCode(),
                t.getSender().getUsername(),
                t.getSender().getFullName(),
                t.getRecipient().getUsername(),
                t.getRecipient().getFullName(),
                t.getSymbol(),
                t.getAmount(),
                t.getAmountBrlEquivalent(),
                t.getFeeBrl(),
                t.getDescription(),
                t.getStatus(),
                t.getCreatedAt()
        )));
    }
}
