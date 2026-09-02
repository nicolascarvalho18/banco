package com.bancosap.controller;

import com.bancosap.dto.request.SupportReplyRequest;
import com.bancosap.dto.request.SupportTicketRequest;
import com.bancosap.dto.response.SupportMessageResponse;
import com.bancosap.dto.response.SupportTicketResponse;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/support")
@Tag(name = "Central de Ajuda & Suporte", description = "Perguntas frequentes, assistente virtual e chamados de suporte")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping("/faq")
    @Operation(summary = "Consultar base de conhecimento FAQ", description = "Retorna lista pública de perguntas e respostas categorizadas.")
    public ResponseEntity<List<Map<String, Object>>> getFaq() {
        return ResponseEntity.ok(supportService.getFaqList());
    }

    @PostMapping("/chatbot")
    @Operation(summary = "Interagir com o assistente virtual", description = "Envia uma pergunta em linguagem natural e recebe resposta automatizada instantânea.")
    public ResponseEntity<Map<String, String>> chatWithBot(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "");
        String answer = supportService.getChatbotAnswer(message);
        return ResponseEntity.ok(Map.of("reply", answer));
    }

    @PostMapping("/tickets")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Abrir chamado de suporte", description = "Cria um novo ticket com número de protocolo exclusivo.")
    public ResponseEntity<SupportTicketResponse> createTicket(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody SupportTicketRequest request) {
        SupportTicketResponse response = supportService.createTicket(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tickets")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Listar chamados do usuário", description = "Retorna o histórico de chamados abertos pelo usuário autenticado.")
    public ResponseEntity<Page<SupportTicketResponse>> getMyTickets(
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(supportService.getUserTickets(user.getId(), pageable));
    }

    @GetMapping("/tickets/{protocol}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Consultar chamado por protocolo", description = "Retorna as mensagens e status do chamado específico.")
    public ResponseEntity<SupportTicketResponse> getTicketByProtocol(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String protocol) {
        return ResponseEntity.ok(supportService.getTicketByProtocol(user.getId(), protocol));
    }

    @PostMapping("/tickets/reply")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Responder a um chamado", description = "Adiciona nova mensagem a um chamado existente.")
    public ResponseEntity<SupportMessageResponse> replyTicket(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody SupportReplyRequest request) {
        return ResponseEntity.ok(supportService.replyTicket(user.getId(), request));
    }
}
