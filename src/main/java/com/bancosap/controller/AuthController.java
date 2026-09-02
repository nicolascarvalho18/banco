package com.bancosap.controller;

import com.bancosap.dto.request.ForgotPasswordRequest;
import com.bancosap.dto.request.LoginRequest;
import com.bancosap.dto.request.RefreshTokenRequest;
import com.bancosap.dto.request.RegisterRequest;
import com.bancosap.dto.request.ResetPasswordRequest;
import com.bancosap.dto.response.AuthResponse;
import com.bancosap.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação & Contas", description = "Endpoints para registro, login, renovação de tokens e recuperação de senha")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Criar nova conta de usuário", description = "Cadastra um novo cliente com saldo demonstrativo inicial e gera tokens de acesso.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.register(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Efetuar login", description = "Autentica usuário por e-mail ou CPF e senha, retornando token JWT.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token de acesso", description = "Emite um novo access token a partir de um refresh token válido.")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperação de senha", description = "Envia código de verificação para o e-mail cadastrado de forma segura.")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        authService.forgotPassword(request, httpRequest);
        return ResponseEntity.ok(Map.of("message", "Se o e-mail informado estiver cadastrado, o código de recuperação foi enviado com sucesso."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha com código", description = "Altera a senha utilizando o código de verificação recebido.")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        authService.resetPassword(request, httpRequest);
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso. Faça login com a nova senha."));
    }
}
