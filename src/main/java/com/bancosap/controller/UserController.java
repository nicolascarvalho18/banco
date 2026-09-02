package com.bancosap.controller;

import com.bancosap.dto.request.ChangePasswordRequest;
import com.bancosap.dto.request.SetPinRequest;
import com.bancosap.dto.request.UpdateProfileRequest;
import com.bancosap.dto.response.UserSummaryResponse;
import com.bancosap.security.UserPrincipal;
import com.bancosap.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Perfil & Configurações de Segurança", description = "Atualização de dados cadastrais, alteração de senha e PIN de transações")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar perfil do usuário autenticado", description = "Retorna dados cadastrais, permissões e status de segurança.")
    public ResponseEntity<UserSummaryResponse> getProfile(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(userService.getProfile(user.getId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Atualizar dados do perfil", description = "Permite alterar nome, telefone, endereço e avatar.")
    public ResponseEntity<UserSummaryResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request, httpRequest));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Alterar senha de acesso", description = "Modifica a senha da conta após validação da senha atual.")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        userService.changePassword(user.getId(), request, httpRequest);
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso."));
    }

    @PostMapping("/set-pin")
    @Operation(summary = "Configurar PIN de transações", description = "Cadastra ou atualiza o PIN de segurança transacional.")
    public ResponseEntity<Map<String, String>> setPin(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody SetPinRequest request,
            HttpServletRequest httpRequest) {
        userService.setTransactionPin(user.getId(), request, httpRequest);
        return ResponseEntity.ok(Map.of("message", "PIN de transações configurado com sucesso."));
    }
}
