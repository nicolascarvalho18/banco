package com.bancosap.controller;

import com.bancosap.dto.request.AdminToggleUserStatusRequest;
import com.bancosap.dto.request.AdminUpdateLimitsRequest;
import com.bancosap.dto.response.AdminDashboardResponse;
import com.bancosap.dto.response.AuditLogResponse;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.dto.response.UserSummaryResponse;
import com.bancosap.enums.AuditAction;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Painel Administrativo & Auditoria", description = "Monitoramento global, gestão de usuários, bloqueios de segurança e logs de auditoria")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Métricas do Dashboard Administrativo", description = "Retorna KPIs gerais (total de usuários, volume 24h, total custodiado, tickets e alertas de segurança).")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardMetrics());
    }

    @GetMapping("/users")
    @Operation(summary = "Consultar usuários", description = "Lista usuários com busca por nome, CPF ou e-mail sem expor credenciais sensíveis.")
    public ResponseEntity<Page<UserSummaryResponse>> getUsers(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(adminService.searchUsers(query, pageable));
    }

    @PatchMapping("/users/status")
    @Operation(summary = "Bloquear ou desbloquear conta de usuário", description = "Modifica o status de acesso do usuário no sistema.")
    public ResponseEntity<UserSummaryResponse> toggleUserStatus(
            @AuthenticationPrincipal UserPrincipal admin,
            @Valid @RequestBody AdminToggleUserStatusRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(adminService.toggleUserStatus(admin.getId(), request, httpRequest));
    }

    @PatchMapping("/users/limits")
    @Operation(summary = "Gerenciar taxas e limites operacionais", description = "Atualiza limites de cheque especial e limites diários/noturnos de PIX.")
    public ResponseEntity<Map<String, String>> updateLimits(
            @AuthenticationPrincipal UserPrincipal admin,
            @Valid @RequestBody AdminUpdateLimitsRequest request,
            HttpServletRequest httpRequest) {
        adminService.updateUserLimits(admin.getId(), request, httpRequest);
        return ResponseEntity.ok(Map.of("message", "Limites atualizados com sucesso."));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Consultar logs de auditoria", description = "Retorna histórico imutável de eventos administrativos e de segurança.")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAuditLogs(action, userEmail, startDate, pageable));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Monitoramento global de transações", description = "Lista todas as transações efetuadas na plataforma.")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllTransactions(pageable));
    }

    @GetMapping("/audit-logs/export-csv")
    @Operation(summary = "Exportar relatório de auditoria", description = "Gera download CSV dos logs de auditoria do sistema.")
    public ResponseEntity<byte[]> exportAuditCsv(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {

        byte[] csv = adminService.exportAuditReportCsv(action, userEmail, startDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio_auditoria_banco_sap.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
