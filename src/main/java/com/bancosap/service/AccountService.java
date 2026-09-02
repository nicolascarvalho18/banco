package com.bancosap.service;

import com.bancosap.dto.request.DepositRequest;
import com.bancosap.dto.response.AccountSummaryResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.Transaction;
import com.bancosap.enums.*;
import com.bancosap.exception.AccountBlockedException;
import com.bancosap.exception.BusinessException;
import com.bancosap.exception.InsufficientBalanceException;
import com.bancosap.exception.ResourceNotFoundException;
import com.bancosap.repository.AccountRepository;
import com.bancosap.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository,
                          AuditService auditService, NotificationService notificationService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public AccountSummaryResponse getAccountSummary(Long userId) {
        Account account = getAccountByUserId(userId);
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();

        BigDecimal monthlyIncome = transactionRepository.sumIncomeSince(account.getId(), startOfMonth);
        BigDecimal monthlyExpenses = transactionRepository.sumExpensesSince(account.getId(), startOfMonth);

        return new AccountSummaryResponse(
                account.getId(),
                account.getAgencyNumber(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getSavingsBalance(),
                account.getCreditLimit(),
                account.getDailyPixLimit(),
                account.getNightlyPixLimit(),
                account.getStatus(),
                monthlyIncome,
                monthlyExpenses
        );
    }

    @Transactional
    public Transaction deposit(Long userId, DepositRequest request, HttpServletRequest httpRequest) {
        Account account = getAccountByUserId(userId);
        if (account.getStatus() != AccountStatus.ATIVO) {
            throw new AccountBlockedException("Conta inativa ou bloqueada para recebimento.");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do depósito deve ser maior que zero.");
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        String method = request.getMethod() != null ? request.getMethod() : "DEPÓSITO SIMULADO";
        Transaction tx = new Transaction(
                null,
                account,
                account.getUser().getFullName(),
                account.getUser().getCpf(),
                "001 - Banco SAP",
                TransactionType.DEPOSITO,
                amount,
                BigDecimal.ZERO,
                TransactionCategory.INVESTIMENTOS,
                "Depósito demonstrativo via " + method
        );
        tx = transactionRepository.save(tx);

        notificationService.createNotification(
                userId,
                "Depósito Confirmado",
                String.format("Você recebeu um depósito de R$ %,.2f via %s.", amount, method),
                NotificationType.SUCCESS
        );

        auditService.logAction(userId, account.getUser().getEmail(), AuditAction.DEPOSIT_MADE, "ACCOUNTS",
                "Depósito de R$ " + amount + " realizado", httpRequest);

        return tx;
    }

    @Transactional
    public void transferToSavings(Long userId, BigDecimal amount, HttpServletRequest httpRequest) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor deve ser maior que zero.");
        }

        Account account = getAccountByUserId(userId);
        if (account.getStatus() != AccountStatus.ATIVO) {
            throw new AccountBlockedException("Conta bloqueada.");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo em conta corrente insuficiente.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.setSavingsBalance(account.getSavingsBalance().add(amount));
        accountRepository.save(account);

        notificationService.createNotification(
                userId,
                "Aplicação em Reserva",
                String.format("R$ %,.2f foram transferidos para a sua Reserva Financeira.", amount),
                NotificationType.SUCCESS
        );
    }

    @Transactional
    public void withdrawFromSavings(Long userId, BigDecimal amount, HttpServletRequest httpRequest) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor deve ser maior que zero.");
        }

        Account account = getAccountByUserId(userId);
        if (account.getStatus() != AccountStatus.ATIVO) {
            throw new AccountBlockedException("Conta bloqueada.");
        }

        if (account.getSavingsBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo na reserva financeira insuficiente.");
        }

        account.setSavingsBalance(account.getSavingsBalance().subtract(amount));
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        notificationService.createNotification(
                userId,
                "Resgate da Reserva",
                String.format("R$ %,.2f foram resgatados da Reserva para a sua Conta Corrente.", amount),
                NotificationType.SUCCESS
        );
    }

    public Account getAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada para o usuário."));
    }
}
