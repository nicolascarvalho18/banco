package com.bancosap.service;

import com.bancosap.dto.request.BillPaymentRequest;
import com.bancosap.dto.response.BillValidationResponse;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.BillPayment;
import com.bancosap.entity.Transaction;
import com.bancosap.entity.User;
import com.bancosap.enums.*;
import com.bancosap.exception.*;
import com.bancosap.repository.AccountRepository;
import com.bancosap.repository.BillPaymentRepository;
import com.bancosap.repository.TransactionRepository;
import com.bancosap.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class BillPaymentService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public BillPaymentService(AccountRepository accountRepository, UserRepository userRepository,
                              BillPaymentRepository billPaymentRepository, TransactionRepository transactionRepository,
                              PasswordEncoder passwordEncoder, NotificationService notificationService,
                              AuditService auditService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.billPaymentRepository = billPaymentRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public BillValidationResponse validateBarcode(String rawBarcode) {
        String clean = rawBarcode.replaceAll("\\D", "");
        if (clean.length() != 44 && clean.length() != 47 && clean.length() != 48) {
            return new BillValidationResponse(rawBarcode, rawBarcode, "Beneficiário Desconhecido",
                    LocalDate.now().plusDays(5), BigDecimal.ZERO, false, "Código de Barras Inválido",
                    "A linha digitável deve ter 44, 47 ou 48 dígitos numéricos.");
        }

        // Simulação de extração de dados do boleto
        String bankCode = clean.substring(0, 3);
        String bankName = switch (bankCode) {
            case "001" -> "Banco do Brasil S.A.";
            case "033" -> "Banco Santander Brasil";
            case "104" -> "Caixa Econômica Federal";
            case "237" -> "Banco Bradesco S.A.";
            case "341" -> "Itaú Unibanco S.A.";
            case "260" -> "Nu Pagamentos S.A.";
            case "777" -> "Banco SAP S.A.";
            default -> "Instituição Financeira Emissora (" + bankCode + ")";
        };

        // Identifica valor simulado a partir dos últimos dígitos ou calcula valor padrão
        BigDecimal amount = new BigDecimal("189.90");
        try {
            if (clean.length() == 47) {
                String valStr = clean.substring(37);
                long valLong = Long.parseLong(valStr);
                if (valLong > 0) {
                    amount = BigDecimal.valueOf(valLong).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception ignored) {}

        String formatted = formatBarcode(clean);
        String recipient = clean.startsWith("8") ? "Concessionária de Serviços Públicos / Telecom" : "Fornecedor / " + bankName;

        return new BillValidationResponse(
                clean,
                formatted,
                recipient,
                LocalDate.now().plusDays(3),
                amount,
                true,
                bankName,
                "Boleto verificado e disponível para pagamento."
        );
    }

    @Transactional
    public TransactionResponse payBill(Long userId, BillPaymentRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        validatePinOrPassword(user, request.getPin());

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));

        if (account.getStatus() != AccountStatus.ATIVO) {
            throw new AccountBlockedException("Sua conta está bloqueada.");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do boleto deve ser maior que zero.");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente para pagar este boleto.");
        }

        // Deduz saldo
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        // Cria registro de pagamento
        BillPayment bill = new BillPayment(
                account,
                request.getBarcode().replaceAll("\\D", ""),
                request.getRecipientName(),
                request.getDueDate(),
                amount
        );
        bill = billPaymentRepository.save(bill);

        // Cria transação correspondente
        Transaction tx = new Transaction(
                account,
                null,
                request.getRecipientName(),
                null,
                "Compensação Bancária CIP",
                TransactionType.PAGAMENTO_BOLETO,
                amount,
                BigDecimal.ZERO,
                TransactionCategory.BOLETO,
                "Pagamento de boleto - " + request.getRecipientName()
        );
        tx.setAuthenticationCode(bill.getAuthenticationCode());
        tx = transactionRepository.save(tx);

        notificationService.createNotification(
                userId,
                "Boleto Pago com Sucesso",
                String.format("Pagamento de R$ %,.2f para %s confirmado.", amount, request.getRecipientName()),
                NotificationType.TRANSACTION
        );

        auditService.logAction(userId, user.getEmail(), AuditAction.BILL_PAID, "BILLS",
                String.format("Boleto de R$ %s pago para %s", amount, request.getRecipientName()), httpRequest);

        return mapToResponse(tx);
    }

    private String formatBarcode(String clean) {
        if (clean.length() == 47) {
            return clean.substring(0, 5) + "." + clean.substring(5, 10) + " " +
                   clean.substring(10, 15) + "." + clean.substring(15, 21) + " " +
                   clean.substring(21, 26) + "." + clean.substring(26, 32) + " " +
                   clean.substring(32, 33) + " " + clean.substring(33);
        }
        return clean;
    }

    private void validatePinOrPassword(User user, String pinOrPass) {
        if (pinOrPass == null || pinOrPass.isBlank()) {
            throw new BusinessException("Informe o PIN para confirmar o pagamento.");
        }
        if (user.getTransactionPinHash() != null) {
            if (!passwordEncoder.matches(pinOrPass, user.getTransactionPinHash()) && !passwordEncoder.matches(pinOrPass, user.getPasswordHash())) {
                throw new BusinessException("PIN de segurança incorreto.");
            }
        } else {
            if (!passwordEncoder.matches(pinOrPass, user.getPasswordHash())) {
                throw new BusinessException("Senha ou PIN incorreto.");
            }
        }
    }

    private TransactionResponse mapToResponse(Transaction tx) {
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
        r.setDestinationBank(tx.getDestinationBank());
        r.setStatus(tx.getStatus());
        r.setCreatedAt(tx.getCreatedAt());
        r.setIncoming(false);
        return r;
    }
}
