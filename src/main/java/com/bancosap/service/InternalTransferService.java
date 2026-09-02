package com.bancosap.service;

import com.bancosap.dto.request.InternalTransferRequest;
import com.bancosap.dto.response.InternalTransferResponse;
import com.bancosap.entity.*;
import com.bancosap.enums.AuditAction;
import com.bancosap.enums.NotificationType;
import com.bancosap.exception.BusinessException;
import com.bancosap.exception.InsufficientBalanceException;
import com.bancosap.exception.ResourceNotFoundException;
import com.bancosap.exception.UnauthorizedException;
import com.bancosap.market.MarketDataService;
import com.bancosap.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class InternalTransferService {

    private final InternalTransferRepository transferRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CryptoWalletRepository walletRepository;
    private final CryptoAssetRepository assetRepository;
    private final MarketDataService marketDataService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public InternalTransferService(InternalTransferRepository transferRepository,
                                   LedgerEntryRepository ledgerRepository,
                                   UserRepository userRepository,
                                   AccountRepository accountRepository,
                                   CryptoWalletRepository walletRepository,
                                   CryptoAssetRepository assetRepository,
                                   MarketDataService marketDataService,
                                   PasswordEncoder passwordEncoder,
                                   NotificationService notificationService,
                                   AuditService auditService) {
        this.transferRepository = transferRepository;
        this.ledgerRepository = ledgerRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;
        this.assetRepository = assetRepository;
        this.marketDataService = marketDataService;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InternalTransferResponse executeTransfer(Long senderId, InternalTransferRequest request, String ipAddress) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário remetente não encontrado."));

        if (sender.getTransactionPinHash() == null) {
            throw new UnauthorizedException("Configure seu PIN de segurança antes de realizar transferências.");
        }

        if (!passwordEncoder.matches(request.getPin(), sender.getTransactionPinHash())) {
            throw new UnauthorizedException("PIN de segurança incorreto.");
        }

        String recipientIdent = request.getRecipientIdentifier().replace("@", "").trim();
        User recipient = userRepository.findByLoginIdentifier(recipientIdent)
                .orElseThrow(() -> new ResourceNotFoundException("Destinatário @" + recipientIdent + " não encontrado na plataforma."));

        if (sender.getId().equals(recipient.getId())) {
            throw new BusinessException("Não é permitido transferir para a sua própria conta.");
        }

        String symbol = request.getSymbol().toUpperCase();
        BigDecimal amount = request.getAmount();
        BigDecimal unitPriceBrl = marketDataService.getPriceInBrl(symbol);
        BigDecimal amountBrl = amount.multiply(unitPriceBrl).setScale(2, RoundingMode.HALF_UP);

        BigDecimal senderBalanceAfter;
        BigDecimal recipientBalanceAfter;

        if ("BRL".equals(symbol)) {
            // Transferência em Reais
            Account senderAcc = accountRepository.findByUserId(sender.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conta do remetente não encontrada."));
            Account recipientAcc = accountRepository.findByUserId(recipient.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conta do destinatário não encontrada."));

            if (senderAcc.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Saldo em reais insuficiente para a transferência.");
            }

            senderAcc.setBalance(senderAcc.getBalance().subtract(amount));
            recipientAcc.setBalance(recipientAcc.getBalance().add(amount));

            accountRepository.save(senderAcc);
            accountRepository.save(recipientAcc);

            senderBalanceAfter = senderAcc.getBalance();
            recipientBalanceAfter = recipientAcc.getBalance();
        } else {
            // Transferência em Criptoativo
            CryptoWallet senderWallet = getOrCreateWallet(sender);
            CryptoWallet recipientWallet = getOrCreateWallet(recipient);

            CryptoAsset senderAsset = assetRepository.findByWalletIdAndSymbol(senderWallet.getId(), symbol)
                    .orElseThrow(() -> new BusinessException("Você não possui saldo de " + symbol + " para transferir."));

            if (senderAsset.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Saldo de " + symbol + " insuficiente.");
            }

            CryptoAsset recipientAsset = assetRepository.findByWalletIdAndSymbol(recipientWallet.getId(), symbol)
                    .orElseGet(() -> new CryptoAsset(recipientWallet, symbol, symbol, BigDecimal.ZERO));

            senderAsset.setBalance(senderAsset.getBalance().subtract(amount));
            recipientAsset.setBalance(recipientAsset.getBalance().add(amount));

            assetRepository.save(senderAsset);
            assetRepository.save(recipientAsset);

            senderBalanceAfter = senderAsset.getBalance();
            recipientBalanceAfter = recipientAsset.getBalance();
        }

        // Registrar Transferência
        InternalTransfer transfer = new InternalTransfer(
                sender,
                recipient,
                symbol,
                amount,
                amountBrl,
                BigDecimal.ZERO,
                request.getDescription()
        );
        InternalTransfer saved = transferRepository.save(transfer);

        // Registrar Ledger de Dupla Entrada
        ledgerRepository.save(new LedgerEntry(
                saved.getAuthenticationCode(),
                sender,
                "DEBITO",
                symbol,
                amount,
                senderBalanceAfter,
                "Transferência enviada para @" + (recipient.getUsername() != null ? recipient.getUsername() : recipient.getFullName())
        ));

        ledgerRepository.save(new LedgerEntry(
                saved.getAuthenticationCode(),
                recipient,
                "CREDITO",
                symbol,
                amount,
                recipientBalanceAfter,
                "Transferência recebida de @" + (sender.getUsername() != null ? sender.getUsername() : sender.getFullName())
        ));

        // Notificações
        notificationService.createNotification(sender.getId(), "Transferência Enviada",
                "Você enviou " + amount + " " + symbol + " para @" + recipient.getUsername(),
                NotificationType.INFO);

        notificationService.createNotification(recipient.getId(), "Transferência Recebida",
                "Você recebeu " + amount + " " + symbol + " de @" + sender.getUsername(),
                NotificationType.INFO);

        auditService.logAction(senderId, sender.getEmail(), AuditAction.TRANSFER_EXECUTED, "internal_transfers", "Para @" + recipient.getUsername(), null);

        return toDto(saved);
    }

    private CryptoWallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    CryptoWallet w = new CryptoWallet(user, "0xSAP" + UUID.randomUUID().toString().replace("-", "").substring(0, 34).toUpperCase());
                    return walletRepository.save(w);
                });
    }

    private InternalTransferResponse toDto(InternalTransfer t) {
        return new InternalTransferResponse(
                t.getAuthenticationCode(),
                t.getSender().getUsername(),
                t.getSender().getFullName(),
                t.getRecipient().getUsername(),
                t.getRecipient().getFullName(),
                t.getSymbol(),
                t.getAmount(),
                t.getAmountBrlEquivalent(),
                t.getFeeBrl(),
                t.getDescription(),
                t.getStatus(),
                t.getCreatedAt()
        );
    }
}
