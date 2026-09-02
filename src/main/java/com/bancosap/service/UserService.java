package com.bancosap.service;

import com.bancosap.dto.request.ChangePasswordRequest;
import com.bancosap.dto.request.SetPinRequest;
import com.bancosap.dto.request.UpdateProfileRequest;
import com.bancosap.dto.response.UserSummaryResponse;
import com.bancosap.entity.User;
import com.bancosap.enums.AuditAction;
import com.bancosap.enums.NotificationType;
import com.bancosap.exception.BusinessException;
import com.bancosap.exception.ResourceNotFoundException;
import com.bancosap.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final AuthService authService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuditService auditService, NotificationService notificationService,
                       AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        return authService.buildUserSummary(user);
    }

    @Transactional
    public UserSummaryResponse updateProfile(Long userId, UpdateProfileRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        user.setAddress(request.getAddress());
        if (request.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }

        user = userRepository.save(user);

        notificationService.createNotification(
                userId,
                "Perfil Atualizado",
                "Seus dados cadastrais foram atualizados com sucesso.",
                NotificationType.INFO
        );

        auditService.logAction(userId, user.getEmail(), AuditAction.LOGIN_SUCCESS, "PROFILE", "Dados de perfil atualizados", httpRequest);

        return authService.buildUserSummary(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("A senha atual informada está incorreta.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        notificationService.createNotification(
                userId,
                "Senha de Acesso Alterada",
                "Sua senha foi alterada com sucesso.",
                NotificationType.SECURITY
        );

        auditService.logAction(userId, user.getEmail(), AuditAction.PASSWORD_CHANGED, "PROFILE", "Senha alterada pelo usuário", httpRequest);
    }

    @Transactional
    public void setTransactionPin(Long userId, SetPinRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Senha da conta incorreta.");
        }

        user.setTransactionPinHash(passwordEncoder.encode(request.getPin()));
        userRepository.save(user);

        notificationService.createNotification(
                userId,
                "PIN de Segurança Definido",
                "Seu PIN de segurança transacional foi cadastrado/atualizado.",
                NotificationType.SECURITY
        );

        auditService.logAction(userId, user.getEmail(), AuditAction.LOGIN_SUCCESS, "PROFILE", "PIN de transações configurado", httpRequest);
    }
}
