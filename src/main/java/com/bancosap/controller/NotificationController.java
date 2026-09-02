package com.bancosap.controller;

import com.bancosap.dto.response.NotificationResponse;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notificações", description = "Central de avisos e notificações de transações e segurança")
@SecurityRequirement(name = "BearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Listar todas as notificações", description = "Retorna o histórico paginado de notificações do usuário.")
    public ResponseEntity<Page<NotificationResponse>> getAll(
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId(), pageable));
    }

    @GetMapping("/recent")
    @Operation(summary = "Listar notificações recentes", description = "Retorna as 5 últimas notificações para o menu dropdown.")
    public ResponseEntity<List<NotificationResponse>> getRecent(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(notificationService.getRecentNotifications(user.getId()));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Contagem de não lidas", description = "Retorna a quantidade de notificações pendentes de leitura.")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(user.getId())));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar notificação como lida", description = "Atualiza o status de leitura de uma notificação específica.")
    public ResponseEntity<Map<String, String>> markAsRead(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long id) {
        notificationService.markAsRead(user.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Notificação marcada como lida."));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marcar todas como lidas", description = "Limpa o contador de notificações não lidas.")
    public ResponseEntity<Map<String, String>> markAllAsRead(@AuthenticationPrincipal UserPrincipal user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of("message", "Todas as notificações foram marcadas como lidas."));
    }
}
