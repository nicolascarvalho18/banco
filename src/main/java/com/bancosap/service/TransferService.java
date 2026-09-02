package com.bancosap.service;

import com.bancosap.dto.request.TransferRequest;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.Transaction;
import com.bancosap.entity.User;
import com.bancosap.enums.*;
import com.bancosap.exception.*;
import com.bancosap.repository.AccountRepository;
import com.bancosap.repository.TransactionRepository;
import com.bancosap.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public TransferService(AccountRepository accountRepository, UserRepository userRepository,
                           TransactionRepository transactionRepository, PasswordEncoder passwordEncoder,
                           NotificationService notificationService, AuditService auditService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse executeTransfer(Long senderUserId, TransferRequest request, HttpServletRequest httpRequest) {
        User senderUser = userRepository.findById(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário pagador não encontrado."));

        validatePinOrPassword(senderUser, request.getPin());

        Account sourceAccount = accountRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta de origem não encontrada."));

        if (sourceAccount.getStatus() != AccountStatus.ATIVO) {
            throw new AccountBlockedException("Sua conta está inativa ou bloqueada para transferências.");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor da transferência deve ser maior que zero.");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(String.format("Saldo insuficiente para transferência. Saldo atual: R$ %,.2f", sourceAccount.getBalance()));
        }

        // Localizar conta de destino
        String destId = request.getDestinationIdentifier().trim();
        Account destinationAccount = findDestinationAccount(destId);

        if (destinationAccount.getId().equals(sourceAccount.getId())) {
            throw new BusinessException("Não é permitido transferir para a própria conta de origem.");
        }

        if (destinationAccount.getStatus() != AccountStatus.ATIVO) {
            throw new BusinessException("A conta do destinatário está inativa ou impossibilitada de receber valores.");
        }

        // Debitar da conta de origem
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        accountRepository.save(sourceAccount);

        // Creditar na conta de destino
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
        accountRepository.save(destinationAccount);

        // Registrar a transação
        Transaction tx = new Transaction(
                sourceAccount,
                destinationAccount,
                destinationAccount.getUser().getFullName(),
                destinationAccount.getUser().getCpf(),
                "001 - Banco SAP",
                TransactionType.TRANSFERENCIA_ENVIADA,
                amount,
                BigDecimal.ZERO,
                request.getCategory() != null ? request.getCategory() : TransactionCategory.TRANSFERENCIA,
                request.getDescription() != null ? request.getDescription() : "Transferência entre contas Banco SAP"
        );
        tx = transactionRepository.save(tx);

        // Notificar remetente
        notificationService.createNotification(
                senderUser.getId(),
                "Transferência Enviada",
                String.format("Você transferiu R$ %,.2f para %s com sucesso.", amount, destinationAccount.getUser().getFullName()),
                NotificationType.TRANSACTION
        );

        // Notificar destinatário
        notificationService.createNotification(
                destinationAccount.getUser().getId(),
                "Transferência Recebida",
                String.format("Você recebeu uma transferência de R$ %,.2f de %s.", amount, senderUser.getFullName()),
                NotificationType.TRANSACTION
        );

        auditService.logAction(senderUserId, senderUser.getEmail(), AuditAction.TRANSFER_EXECUTED, "TRANSFERS",
                String.format("Transferência de R$ %s para conta %s", amount, destinationAccount.getAccountNumber()), httpRequest);

        return mapToResponse(tx, false);
    }

    private Account findDestinationAccount(String identifier) {
        String cleanIdentifier = identifier.replaceAll("\\D", "");

        // 1. Tentar por CPF
        if (cleanIdentifier.length() == 11) {
            String formattedCpf = cleanIdentifier.substring(0, 3) + "." + cleanIdentifier.substring(3, 6) + "." + cleanIdentifier.substring(6, 9) + "-" + cleanIdentifier.substring(9, 11);
            Account acc = accountRepository.findByUserCpf(formattedCpf)
                    .or(() -> accountRepository.findByUserCpf(cleanIdentifier))
                    .orElse(null);
            if (acc != null) return acc;
        }

        // 2. Tentar por e-mail
        if (identifier.contains("@")) {
            Account acc = accountRepository.findByUserEmail(identifier.toLowerCase()).orElse(null);
            if (acc != null) return acc;
        }

        // 3. Tentar por número da conta (ex: 33458-1 ou 334581)
        Account acc = accountRepository.findByAccountNumber(identifier)
                .or(() -> accountRepository.findByAccountNumber(identifier.replace("-", "")))
                .orElse(null);
        if (acc != null) return acc;

        throw new ResourceNotFoundException("Conta de destino não encontrada para o identificador informado: " + identifier);
    }

    private void validatePinOrPassword(User user, String pinOrPass) {
        if (pinOrPass == null || pinOrPass.isBlank()) {
            throw new BusinessException("Informe o seu PIN de 4 a 6 dígitos para confirmar a transação.");
        }

        if (user.getTransactionPinHash() != null) {
            if (!passwordEncoder.matches(pinOrPass, user.getTransactionPinHash()) && !passwordEncoder.matches(pinOrPass, user.getPasswordHash())) {
                throw new BusinessException("PIN de segurança incorreto.");
            }
        } else {
            // Se ainda não cadastrou PIN, valida com a senha do usuário
            if (!passwordEncoder.matches(pinOrPass, user.getPasswordHash())) {
                throw new BusinessException("Senha ou PIN incorreto.");
            }
        }
    }

    private TransactionResponse mapToResponse(Transaction tx, boolean incoming) {
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
        r.setIncoming(incoming);
        return r;
    }
}
