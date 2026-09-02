package com.bancosap.service;

import com.bancosap.dto.request.AdminToggleUserStatusRequest;
import com.bancosap.dto.request.AdminUpdateLimitsRequest;
import com.bancosap.dto.response.AdminDashboardResponse;
import com.bancosap.dto.response.AuditLogResponse;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.dto.response.UserSummaryResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.AuditLog;
import com.bancosap.entity.Transaction;
import com.bancosap.entity.User;
import com.bancosap.enums.AuditAction;
import com.bancosap.enums.NotificationType;
import com.bancosap.enums.RoleName;
import com.bancosap.enums.TicketStatus;
import com.bancosap.enums.UserStatus;
import com.bancosap.exception.BusinessException;
import com.bancosap.exception.ResourceNotFoundException;
import com.bancosap.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final SupportTicketRepository ticketRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public AdminService(UserRepository userRepository, AccountRepository accountRepository,
                        TransactionRepository transactionRepository, AuditLogRepository auditLogRepository,
                        SupportTicketRepository ticketRepository, AuditService auditService,
                        NotificationService notificationService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
        this.ticketRepository = ticketRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardMetrics() {
        long totalUsers = userRepository.count();
        List<User> users = userRepository.findAll();
        long activeUsers = users.stream().filter(u -> u.getStatus() == UserStatus.ATIVO).count();
        long blockedUsers = users.stream().filter(u -> u.getStatus() == UserStatus.BLOQUEADO).count();

        List<Account> accounts = accountRepository.findAll();
        BigDecimal totalDepositBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTransactionsCount = transactionRepository.count();
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        BigDecimal totalVolume24h = transactionRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().isAfter(yesterday))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingTickets = ticketRepository.count();
        long securityAlertsCount = auditLogRepository.findAll().stream()
                .filter(a -> a.getAction() == AuditAction.LOGIN_FAILED || a.getAction() == AuditAction.ACCOUNT_LOCKED)
                .count();

        return new AdminDashboardResponse(
                totalUsers,
                activeUsers,
                blockedUsers,
                totalDepositBalance,
                totalVolume24h,
                totalTransactionsCount,
                pendingTickets,
                securityAlertsCount
        );
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> searchUsers(String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return userRepository.searchUsers(query.trim(), pageable).map(this::mapToUserSummary);
        }
        return userRepository.findAll(pageable).map(this::mapToUserSummary);
    }

    @Transactional
    public UserSummaryResponse toggleUserStatus(Long adminId, AdminToggleUserStatusRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (user.getRole() == RoleName.ROLE_ADMIN && request.getStatus() == UserStatus.BLOQUEADO) {
            throw new BusinessException("Não é permitido bloquear uma conta com perfil de Administrador.");
        }

        user.setStatus(request.getStatus());
        if (request.getStatus() == UserStatus.ATIVO) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }
        user = userRepository.save(user);

        User admin = userRepository.findById(adminId).orElse(null);
        String adminEmail = admin != null ? admin.getEmail() : "admin@bancosap.com.br";

        AuditAction action = request.getStatus() == UserStatus.BLOQUEADO ? AuditAction.USER_BLOCKED_ADMIN : AuditAction.USER_UNBLOCKED_ADMIN;
        auditService.logAction(adminId, adminEmail, action, "ADMIN_USERS",
                String.format("Status do usuário %s alterado para %s. Motivo: %s", user.getEmail(), request.getStatus(), request.getReason()), httpRequest);

        notificationService.createNotification(
                user.getId(),
                "Status da Conta Alterado",
                String.format("O status da sua conta foi atualizado para: %s.", request.getStatus()),
                NotificationType.SECURITY
        );

        return mapToUserSummary(user);
    }

    @Transactional
    public void updateUserLimits(Long adminId, AdminUpdateLimitsRequest request, HttpServletRequest httpRequest) {
        Account account = accountRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta do usuário não encontrada."));

        account.setCreditLimit(request.getCreditLimit());
        account.setDailyPixLimit(request.getDailyPixLimit());
        account.setNightlyPixLimit(request.getNightlyPixLimit());
        accountRepository.save(account);

        User admin = userRepository.findById(adminId).orElse(null);
        String adminEmail = admin != null ? admin.getEmail() : "admin@bancosap.com.br";

        auditService.logAction(adminId, adminEmail, AuditAction.ADMIN_LIMIT_UPDATED, "ADMIN_LIMITS",
                String.format("Limites da conta %s atualizados (Crédito: R$ %s, PIX Diário: R$ %s)", account.getAccountNumber(), request.getCreditLimit(), request.getDailyPixLimit()), httpRequest);

        notificationService.createNotification(
                request.getUserId(),
                "Limites de Conta Atualizados",
                String.format("Seus novos limites foram aprovados: Crédito: R$ %,.2f | PIX Diário: R$ %,.2f", request.getCreditLimit(), request.getDailyPixLimit()),
                NotificationType.INFO
        );
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(AuditAction action, String userEmail, LocalDateTime startDate, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(action, userEmail, startDate, pageable)
                .map(this::mapToAuditResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(tx -> {
            TransactionResponse r = new TransactionResponse();
            r.setId(tx.getId());
            r.setAuthenticationCode(tx.getAuthenticationCode());
            r.setType(tx.getTransactionType());
            r.setTypeDescription(tx.getTransactionType().name());
            r.setAmount(tx.getAmount());
            r.setFee(tx.getFee());
            r.setCategory(tx.getCategory());
            r.setDescription(tx.getDescription());
            r.setDestinationName(tx.getDestinationName());
            r.setDestinationDocument(tx.getDestinationDocument());
            r.setDestinationBank(tx.getDestinationBank());
            if (tx.getSourceAccount() != null && tx.getSourceAccount().getUser() != null) {
                r.setSourceName(tx.getSourceAccount().getUser().getFullName());
                r.setSourceAccountNumber(tx.getSourceAccount().getAccountNumber());
            }
            r.setStatus(tx.getStatus());
            r.setCreatedAt(tx.getCreatedAt());
            r.setIncoming(false);
            return r;
        });
    }

    @Transactional(readOnly = true)
    public byte[] exportAuditReportCsv(AuditAction action, String userEmail, LocalDateTime startDate) {
        Page<AuditLog> page = auditLogRepository.searchAuditLogs(action, userEmail, startDate, Pageable.unpaged());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);

        writer.println("Data/Hora;ID Usuário;E-mail;Ação;Recurso;IP Origem;Detalhes");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (AuditLog log : page.getContent()) {
            writer.printf("%s;%s;%s;%s;%s;%s;\"%s\"%n",
                    log.getCreatedAt().format(dtf),
                    log.getUserId() != null ? log.getUserId() : "",
                    log.getUserEmail() != null ? log.getUserEmail() : "",
                    log.getAction().name(),
                    log.getResource() != null ? log.getResource() : "",
                    log.getIpAddress() != null ? log.getIpAddress() : "",
                    log.getDetails() != null ? log.getDetails().replace("\"", "'") : ""
            );
        }

        writer.flush();
        return out.toByteArray();
    }

    private UserSummaryResponse mapToUserSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                maskCpf(user.getCpf()),
                user.getBirthDate(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getProfilePhotoUrl(),
                user.getAddress(),
                user.getTransactionPinHash() != null
        );
    }

    private AuditLogResponse mapToAuditResponse(AuditLog a) {
        return new AuditLogResponse(
                a.getId(),
                a.getUserId(),
                a.getUserEmail(),
                a.getAction(),
                a.getResource(),
                a.getIpAddress(),
                a.getUserAgent(),
                a.getDetails(),
                a.getCreatedAt()
        );
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) return "***.***.***-**";
        String clean = cpf.replaceAll("\\D", "");
        if (clean.length() == 11) {
            return clean.substring(0, 3) + ".***.***-" + clean.substring(9);
        }
        return cpf;
    }
}
