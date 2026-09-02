package com.bancosap.service;

import com.bancosap.dto.request.CreateVirtualCardRequest;
import com.bancosap.dto.request.SimulatePurchaseRequest;
import com.bancosap.dto.request.ToggleCardStatusRequest;
import com.bancosap.dto.request.UpdateCardLimitRequest;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.dto.response.VirtualCardResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.Transaction;
import com.bancosap.entity.User;
import com.bancosap.entity.VirtualCard;
import com.bancosap.enums.*;
import com.bancosap.exception.*;
import com.bancosap.repository.AccountRepository;
import com.bancosap.repository.TransactionRepository;
import com.bancosap.repository.UserRepository;
import com.bancosap.repository.VirtualCardRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final VirtualCardRepository virtualCardRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    public CardService(VirtualCardRepository virtualCardRepository, AccountRepository accountRepository,
                       UserRepository userRepository, TransactionRepository transactionRepository,
                       PasswordEncoder passwordEncoder, NotificationService notificationService,
                       AuditService auditService) {
        this.virtualCardRepository = virtualCardRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<VirtualCardResponse> getAccountCards(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));
        return virtualCardRepository.findByAccountId(account.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VirtualCardResponse createVirtualCard(Long userId, CreateVirtualCardRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        validatePinOrPassword(user, request.getPin());

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));

        if (account.getStatus() != AccountStatus.ATIVO) {
            throw new AccountBlockedException("Sua conta está inativa ou bloqueada.");
        }

        String rawNumber = generateCardNumber();
        String masked = "•••• •••• •••• " + rawNumber.substring(12);
        String expiry = LocalDate.now().plusYears(4).format(DateTimeFormatter.ofPattern("MM/yy"));
        String cvv = String.format("%03d", secureRandom.nextInt(900) + 100);

        VirtualCard card = new VirtualCard(
                account,
                masked,
                rawNumber,
                request.getHolderName().toUpperCase().trim(),
                expiry,
                cvv,
                CardType.VIRTUAL,
                request.getSpendingLimit(),
                request.isTemporary()
        );

        card = virtualCardRepository.save(card);

        notificationService.createNotification(
                userId,
                "Novo Cartão Virtual Gerado",
                String.format("Seu cartão virtual com final %s foi criado com limite de R$ %,.2f.", rawNumber.substring(12), request.getSpendingLimit()),
                NotificationType.SUCCESS
        );

        auditService.logAction(userId, user.getEmail(), AuditAction.CARD_CREATED, "CARDS",
                "Cartão virtual criado (final " + rawNumber.substring(12) + ")", httpRequest);

        return mapToResponse(card);
    }

    @Transactional
    public VirtualCardResponse toggleCardStatus(Long userId, ToggleCardStatusRequest request, HttpServletRequest httpRequest) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));

        VirtualCard card = virtualCardRepository.findByIdAndAccountId(request.getCardId(), account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cartão não encontrado."));

        card.setStatus(request.getStatus());
        card = virtualCardRepository.save(card);

        auditService.logAction(userId, account.getUser().getEmail(), AuditAction.CARD_STATUS_CHANGED, "CARDS",
                "Status do cartão " + card.getCardNumberMasked() + " alterado para " + request.getStatus(), httpRequest);

        return mapToResponse(card);
    }

    @Transactional
    public VirtualCardResponse updateSpendingLimit(Long userId, UpdateCardLimitRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        validatePinOrPassword(user, request.getPin());

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));

        VirtualCard card = virtualCardRepository.findByIdAndAccountId(request.getCardId(), account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cartão não encontrado."));

        card.setSpendingLimit(request.getNewLimit());
        card = virtualCardRepository.save(card);

        auditService.logAction(userId, user.getEmail(), AuditAction.CARD_LIMIT_UPDATED, "CARDS",
                "Limite do cartão " + card.getCardNumberMasked() + " ajustado para R$ " + request.getNewLimit(), httpRequest);

        return mapToResponse(card);
    }

    @Transactional
    public TransactionResponse simulatePurchase(Long userId, SimulatePurchaseRequest request, HttpServletRequest httpRequest) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));

        VirtualCard card = virtualCardRepository.findByCardNumberToken(request.getCardNumberToken())
                .orElseThrow(() -> new BusinessException("Dados do cartão inválidos ou não reconhecidos."));

        if (!card.getAccount().getId().equals(account.getId())) {
            throw new UnauthorizedException("Cartão não pertence a este usuário.");
        }

        if (card.getStatus() != CardStatus.ATIVO) {
            throw new BusinessException("Transação negada: o cartão está bloqueado no aplicativo.");
        }

        if (!card.getCvvSimulated().equals(request.getCvv().trim())) {
            throw new BusinessException("Transação negada: CVV incorreto.");
        }

        BigDecimal availableLimit = card.getSpendingLimit().subtract(card.getUsedLimit());
        if (request.getAmount().compareTo(availableLimit) > 0) {
            throw new InsufficientBalanceException("Transação negada: Limite disponível insuficiente no cartão.");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Transação negada: Saldo em conta corrente insuficiente.");
        }

        // Deduz do saldo e atualiza limite utilizado do cartão
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        card.setUsedLimit(card.getUsedLimit().add(request.getAmount()));

        accountRepository.save(account);
        virtualCardRepository.save(card);

        Transaction tx = new Transaction(
                account,
                null,
                request.getMerchantName(),
                null,
                "Rede Adquirente SAP",
                TransactionType.COMPRA_CARTAO,
                request.getAmount(),
                BigDecimal.ZERO,
                TransactionCategory.SERVICOS,
                "Compra com cartão (" + card.getCardNumberMasked() + ") em " + request.getMerchantName()
        );
        tx = transactionRepository.save(tx);

        notificationService.createNotification(
                userId,
                "Compra Aprovada no Cartão",
                String.format("Compra de R$ %,.2f em %s aprovada.", request.getAmount(), request.getMerchantName()),
                NotificationType.TRANSACTION
        );

        auditService.logAction(userId, account.getUser().getEmail(), AuditAction.CARD_PURCHASE_SIMULATED, "CARDS",
                "Compra de R$ " + request.getAmount() + " em " + request.getMerchantName(), httpRequest);

        return mapToTransactionResponse(tx);
    }

    private void validatePinOrPassword(User user, String pinOrPass) {
        if (pinOrPass == null || pinOrPass.isBlank()) {
            throw new BusinessException("Informe o PIN de segurança para autorizar.");
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

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    private VirtualCardResponse mapToResponse(VirtualCard c) {
        return new VirtualCardResponse(
                c.getId(),
                c.getCardNumberMasked(),
                c.getCardNumberToken(),
                c.getHolderName(),
                c.getExpirationDate(),
                c.getCvvSimulated(),
                c.getCardType(),
                c.getStatus(),
                c.getSpendingLimit(),
                c.getUsedLimit(),
                c.isTemporary(),
                c.getCreatedAt()
        );
    }

    private TransactionResponse mapToTransactionResponse(Transaction tx) {
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
