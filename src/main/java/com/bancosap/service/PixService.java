package com.bancosap.service;

import com.bancosap.dto.request.PixKeyCreateRequest;
import com.bancosap.dto.request.PixSendRequest;
import com.bancosap.dto.response.PixKeyResponse;
import com.bancosap.dto.response.PixQrCodeResponse;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.PixKey;
import com.bancosap.entity.Transaction;
import com.bancosap.entity.User;
import com.bancosap.enums.*;
import com.bancosap.exception.*;
import com.bancosap.repository.AccountRepository;
import com.bancosap.repository.PixKeyRepository;
import com.bancosap.repository.TransactionRepository;
import com.bancosap.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PixService {

    private final PixKeyRepository pixKeyRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PixService(PixKeyRepository pixKeyRepository, AccountRepository accountRepository,
                      UserRepository userRepository, TransactionRepository transactionRepository,
                      PasswordEncoder passwordEncoder, NotificationService notificationService,
                      AuditService auditService) {
        this.pixKeyRepository = pixKeyRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<PixKeyResponse> getUserPixKeys(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));
        return pixKeyRepository.findByAccountId(account.getId()).stream()
                .map(k -> new PixKeyResponse(k.getId(), k.getKeyType(), k.getKeyValue(), k.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public PixKeyResponse createPixKey(Long userId, PixKeyCreateRequest request, HttpServletRequest httpRequest) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));

        if (pixKeyRepository.countByAccount(account) >= 5) {
            throw new BusinessException("Limite máximo de 5 chaves PIX por conta atingido.");
        }

        String keyValue = request.getKeyValue().trim();
        if (request.getKeyType() == PixKeyType.ALEATORIA) {
            keyValue = UUID.randomUUID().toString();
        }

        if (pixKeyRepository.existsByKeyValue(keyValue)) {
            throw new DuplicateOperationException("Esta chave PIX já está vinculada a uma conta.");
        }

        PixKey pixKey = new PixKey(account, request.getKeyType(), keyValue);
        pixKey = pixKeyRepository.save(pixKey);

        auditService.logAction(userId, account.getUser().getEmail(), AuditAction.PIX_KEY_CREATED, "PIX",
                "Chave PIX " + request.getKeyType() + " criada", httpRequest);

        return new PixKeyResponse(pixKey.getId(), pixKey.getKeyType(), pixKey.getKeyValue(), pixKey.getCreatedAt());
    }

    @Transactional
    public void deletePixKey(Long userId, Long keyId, HttpServletRequest httpRequest) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));

        PixKey key = pixKeyRepository.findById(keyId)
                .filter(k -> k.getAccount().getId().equals(account.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Chave PIX não encontrada."));

        pixKeyRepository.delete(key);
        auditService.logAction(userId, account.getUser().getEmail(), AuditAction.PIX_KEY_DELETED, "PIX",
                "Chave PIX deletada", httpRequest);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse sendPix(Long senderUserId, PixSendRequest request, HttpServletRequest httpRequest) {
        User senderUser = userRepository.findById(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        validatePinOrPassword(senderUser, request.getPin());

        Account sourceAccount = accountRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta de origem não encontrada."));

        if (sourceAccount.getStatus() != AccountStatus.ATIVO) {
            throw new AccountBlockedException("Sua conta está bloqueada.");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do PIX deve ser maior que zero.");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente para realizar o PIX.");
        }

        // Validação de limite diário
        if (amount.compareTo(sourceAccount.getDailyPixLimit()) > 0) {
            throw new BusinessException(String.format("Valor excede seu limite diário PIX de R$ %,.2f.", sourceAccount.getDailyPixLimit()));
        }

        // Localizar chave PIX ou conta correspondente
        String rawKey = request.getKeyValue().trim();
        Account destinationAccount = null;

        String targetKey = rawKey;
        if (rawKey.startsWith("00020126")) {
            targetKey = extractKeyFromPixPayload(rawKey);
        }

        final String finalTargetKey = targetKey;
        PixKey pixKey = pixKeyRepository.findByKeyValue(finalTargetKey).orElse(null);
        if (pixKey != null) {
            destinationAccount = pixKey.getAccount();
        } else {
            // Tentar localizar por CPF ou Email direto
            destinationAccount = accountRepository.findByUserCpf(finalTargetKey)
                    .or(() -> accountRepository.findByUserEmail(finalTargetKey))
                    .orElse(null);
        }

        if (destinationAccount == null) {
            throw new InvalidPixKeyException("Chave PIX não encontrada no sistema demonstrativo Banco SAP.");
        }

        if (destinationAccount.getId().equals(sourceAccount.getId())) {
            throw new BusinessException("Não é permitido enviar PIX para a própria conta.");
        }

        // Debitar da origem
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        accountRepository.save(sourceAccount);

        // Creditar no destino
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
        accountRepository.save(destinationAccount);

        Transaction tx = new Transaction(
                sourceAccount,
                destinationAccount,
                destinationAccount.getUser().getFullName(),
                destinationAccount.getUser().getCpf(),
                "001 - Banco SAP",
                TransactionType.PIX_ENVIADO,
                amount,
                BigDecimal.ZERO,
                request.getCategory() != null ? request.getCategory() : TransactionCategory.PIX,
                request.getDescription() != null ? request.getDescription() : "PIX enviado para " + rawKey
        );
        tx = transactionRepository.save(tx);

        // Notificações
        notificationService.createNotification(
                senderUser.getId(),
                "PIX Enviado",
                String.format("Você enviou um PIX de R$ %,.2f para %s.", amount, destinationAccount.getUser().getFullName()),
                NotificationType.TRANSACTION
        );

        notificationService.createNotification(
                destinationAccount.getUser().getId(),
                "PIX Recebido",
                String.format("Você recebeu um PIX de R$ %,.2f de %s.", amount, senderUser.getFullName()),
                NotificationType.TRANSACTION
        );

        auditService.logAction(senderUserId, senderUser.getEmail(), AuditAction.PIX_SENT, "PIX",
                String.format("PIX de R$ %s enviado para %s", amount, rawKey), httpRequest);

        return mapToResponse(tx, false);
    }

    public PixQrCodeResponse generateQrCode(Long userId, BigDecimal amount, String customKey) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));

        String keyToUse = customKey;
        if (keyToUse == null || keyToUse.isBlank()) {
            List<PixKey> keys = pixKeyRepository.findByAccountId(account.getId());
            if (!keys.isEmpty()) {
                keyToUse = keys.get(0).getKeyValue();
            } else {
                keyToUse = account.getUser().getCpf();
            }
        }

        BigDecimal amt = amount != null ? amount : BigDecimal.ZERO;
        String payload = String.format("00020126580014BR.GOV.BCB.PIX01%02d%s52040000530398654%05.2f5802BR59%02d%s6009SAO PAULO62070503***6304%04X",
                keyToUse.length(), keyToUse, amt,
                account.getUser().getFullName().length(), account.getUser().getFullName().toUpperCase(),
                secureRandom.nextInt(0xFFFF));

        // Mock visual base64 QR Code SVG representation
        String qrSvg = "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 200 200' width='200' height='200'><rect width='200' height='200' fill='#ffffff'/><rect x='20' y='20' width='40' height='40' fill='#0A192F'/><rect x='30' y='30' width='20' height='20' fill='#ffffff'/><rect x='140' y='20' width='40' height='40' fill='#0A192F'/><rect x='150' y='30' width='20' height='20' fill='#ffffff'/><rect x='20' y='140' width='40' height='40' fill='#0A192F'/><rect x='30' y='150' width='20' height='20' fill='#ffffff'/><rect x='80' y='80' width='40' height='40' fill='#10B981'/><rect x='90' y='30' width='20' height='30' fill='#0A192F'/><rect x='140' y='90' width='30' height='20' fill='#0A192F'/><rect x='30' y='90' width='30' height='20' fill='#0A192F'/><rect x='90' y='140' width='20' height='40' fill='#0A192F'/><rect x='140' y='140' width='30' height='30' fill='#0A192F'/></svg>";
        String base64Svg = "data:image/svg+xml;utf8," + java.net.URLEncoder.encode(qrSvg, java.nio.charset.StandardCharsets.UTF_8);

        return new PixQrCodeResponse(
                payload,
                base64Svg,
                amt,
                account.getUser().getFullName(),
                keyToUse,
                "São Paulo/SP"
        );
    }

    private String extractKeyFromPixPayload(String payload) {
        try {
            int keyIndex = payload.indexOf("BR.GOV.BCB.PIX01");
            if (keyIndex != -1) {
                int lenStart = keyIndex + "BR.GOV.BCB.PIX01".length();
                int length = Integer.parseInt(payload.substring(lenStart, lenStart + 2));
                return payload.substring(lenStart + 2, lenStart + 2 + length);
            }
        } catch (Exception ignored) {}
        return payload;
    }

    private void validatePinOrPassword(User user, String pinOrPass) {
        if (pinOrPass == null || pinOrPass.isBlank()) {
            throw new BusinessException("Informe o PIN de segurança para validar a transação.");
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
