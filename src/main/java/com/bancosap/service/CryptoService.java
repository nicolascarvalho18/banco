package com.bancosap.service;

import com.bancosap.dto.request.CryptoTradeRequest;
import com.bancosap.dto.request.CryptoTransferRequest;
import com.bancosap.dto.response.*;
import com.bancosap.entity.*;
import com.bancosap.enums.*;
import com.bancosap.exception.*;
import com.bancosap.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CryptoService {

    private final CryptoWalletRepository cryptoWalletRepository;
    private final CryptoAssetRepository cryptoAssetRepository;
    private final CryptoTransactionRepository cryptoTransactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    // Cotações base simuladas em Reais (BRL)
    private static final Map<CryptoSymbol, BigDecimal> BASE_PRICES = Map.of(
            CryptoSymbol.BTC, new BigDecimal("345200.00"),
            CryptoSymbol.ETH, new BigDecimal("18450.00"),
            CryptoSymbol.SOL, new BigDecimal("890.50"),
            CryptoSymbol.USDT, new BigDecimal("5.65"),
            CryptoSymbol.ADA, new BigDecimal("3.42")
    );

    public CryptoService(CryptoWalletRepository cryptoWalletRepository, CryptoAssetRepository cryptoAssetRepository,
                         CryptoTransactionRepository cryptoTransactionRepository, AccountRepository accountRepository,
                         UserRepository userRepository, PasswordEncoder passwordEncoder,
                         NotificationService notificationService, AuditService auditService) {
        this.cryptoWalletRepository = cryptoWalletRepository;
        this.cryptoAssetRepository = cryptoAssetRepository;
        this.cryptoTransactionRepository = cryptoTransactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public List<CryptoQuoteResponse> getMarketQuotes() {
        List<CryptoQuoteResponse> quotes = new ArrayList<>();
        for (CryptoSymbol symbol : CryptoSymbol.values()) {
            BigDecimal base = BASE_PRICES.get(symbol);
            // Flutuação simulada sutil (-2.5% a +3.8%)
            double variationPercent = -2.5 + (secureRandom.nextDouble() * 6.3);
            BigDecimal currentPrice = base.multiply(BigDecimal.valueOf(1.0 + (variationPercent / 100.0)))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal high = currentPrice.multiply(BigDecimal.valueOf(1.03)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal low = currentPrice.multiply(BigDecimal.valueOf(0.97)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal volume = currentPrice.multiply(BigDecimal.valueOf(1500 + secureRandom.nextInt(5000))).setScale(2, RoundingMode.HALF_UP);

            quotes.add(new CryptoQuoteResponse(
                    symbol.name(),
                    symbol.getFullName(),
                    currentPrice,
                    BigDecimal.valueOf(variationPercent).setScale(2, RoundingMode.HALF_UP),
                    high,
                    low,
                    volume
            ));
        }
        return quotes;
    }

    @Transactional(readOnly = true)
    public CryptoPortfolioResponse getPortfolio(Long userId) {
        CryptoWallet wallet = getOrCreateWallet(userId);
        List<CryptoAsset> assets = cryptoAssetRepository.findByWalletId(wallet.getId());

        List<CryptoQuoteResponse> quotes = getMarketQuotes();
        Map<String, BigDecimal> quoteMap = quotes.stream()
                .collect(Collectors.toMap(CryptoQuoteResponse::getSymbol, CryptoQuoteResponse::getPriceBrl));
        Map<String, BigDecimal> changeMap = quotes.stream()
                .collect(Collectors.toMap(CryptoQuoteResponse::getSymbol, CryptoQuoteResponse::getChange24hPercent));

        BigDecimal totalPortfolioBrl = BigDecimal.ZERO;
        List<CryptoAssetResponse> assetResponses = new ArrayList<>();

        for (CryptoAsset asset : assets) {
            BigDecimal unitPrice = quoteMap.getOrDefault(asset.getSymbol(), BASE_PRICES.get(CryptoSymbol.valueOf(asset.getSymbol())));
            BigDecimal totalAssetBrl = asset.getBalance().multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            totalPortfolioBrl = totalPortfolioBrl.add(totalAssetBrl);

            assetResponses.add(new CryptoAssetResponse(
                    asset.getSymbol(),
                    asset.getName(),
                    asset.getBalance(),
                    unitPrice,
                    totalAssetBrl,
                    changeMap.getOrDefault(asset.getSymbol(), BigDecimal.ZERO)
            ));
        }

        List<CryptoTransactionResponse> recentTxs = cryptoTransactionRepository.findAllByWalletId(wallet.getId(), PageRequest.of(0, 10))
                .getContent().stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());

        return new CryptoPortfolioResponse(wallet.getWalletAddress(), totalPortfolioBrl, assetResponses, recentTxs);
    }

    @Transactional
    public CryptoTransactionResponse trade(Long userId, CryptoTradeRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        validatePinOrPassword(user, request.getPin());

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta corrente não encontrada."));

        if (account.getStatus() != AccountStatus.ATIVO) {
            throw new AccountBlockedException("Conta bloqueada para transações.");
        }

        CryptoWallet wallet = getOrCreateWallet(userId);
        CryptoAsset asset = cryptoAssetRepository.findByWalletIdAndSymbol(wallet.getId(), request.getSymbol().name())
                .orElseGet(() -> cryptoAssetRepository.save(new CryptoAsset(wallet, request.getSymbol().name(), request.getSymbol().getFullName(), BigDecimal.ZERO)));

        BigDecimal unitPrice = BASE_PRICES.get(request.getSymbol());
        BigDecimal amountBrl = request.getAmountBrl();
        BigDecimal cryptoQty = amountBrl.divide(unitPrice, 8, RoundingMode.HALF_UP);

        if (request.getOperationType() == CryptoOperationType.COMPRA) {
            if (account.getBalance().compareTo(amountBrl) < 0) {
                throw new InsufficientBalanceException("Saldo em reais insuficiente para realizar a compra do ativo.");
            }

            // Deduz reais e adiciona cripto
            account.setBalance(account.getBalance().subtract(amountBrl));
            asset.setBalance(asset.getBalance().add(cryptoQty));

            accountRepository.save(account);
            cryptoAssetRepository.save(asset);

            String txHash = generateTxHash();
            CryptoTransaction tx = new CryptoTransaction(
                    txHash,
                    null,
                    wallet,
                    request.getSymbol().name(),
                    cryptoQty,
                    unitPrice,
                    amountBrl,
                    CryptoOperationType.COMPRA
            );
            tx = cryptoTransactionRepository.save(tx);

            notificationService.createNotification(
                    userId,
                    "Compra de Criptoativo Confirmada",
                    String.format("Você comprou %s %s por R$ %,.2f.", cryptoQty.stripTrailingZeros().toPlainString(), request.getSymbol().name(), amountBrl),
                    NotificationType.TRANSACTION
            );

            auditService.logAction(userId, user.getEmail(), AuditAction.CRYPTO_BOUGHT, "CRYPTO",
                    String.format("Compra de %s %s", cryptoQty, request.getSymbol()), httpRequest);

            return mapToTransactionResponse(tx);

        } else if (request.getOperationType() == CryptoOperationType.VENDA) {
            if (asset.getBalance().compareTo(cryptoQty) < 0) {
                throw new InsufficientBalanceException(String.format("Saldo insuficiente de %s na carteira.", request.getSymbol()));
            }

            // Deduz cripto e adiciona reais
            asset.setBalance(asset.getBalance().subtract(cryptoQty));
            account.setBalance(account.getBalance().add(amountBrl));

            cryptoAssetRepository.save(asset);
            accountRepository.save(account);

            String txHash = generateTxHash();
            CryptoTransaction tx = new CryptoTransaction(
                    txHash,
                    wallet,
                    null,
                    request.getSymbol().name(),
                    cryptoQty,
                    unitPrice,
                    amountBrl,
                    CryptoOperationType.VENDA
            );
            tx = cryptoTransactionRepository.save(tx);

            notificationService.createNotification(
                    userId,
                    "Venda de Criptoativo Confirmada",
                    String.format("Você vendeu %s %s por R$ %,.2f.", cryptoQty.stripTrailingZeros().toPlainString(), request.getSymbol().name(), amountBrl),
                    NotificationType.TRANSACTION
            );

            auditService.logAction(userId, user.getEmail(), AuditAction.CRYPTO_SOLD, "CRYPTO",
                    String.format("Venda de %s %s", cryptoQty, request.getSymbol()), httpRequest);

            return mapToTransactionResponse(tx);
        } else {
            throw new BusinessException("Tipo de operação inválido para negociação.");
        }
    }

    @Transactional
    public CryptoTransactionResponse transferP2P(Long senderUserId, CryptoTransferRequest request, HttpServletRequest httpRequest) {
        User senderUser = userRepository.findById(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        validatePinOrPassword(senderUser, request.getPin());

        CryptoWallet senderWallet = getOrCreateWallet(senderUserId);
        CryptoAsset senderAsset = cryptoAssetRepository.findByWalletIdAndSymbol(senderWallet.getId(), request.getSymbol().name())
                .orElseThrow(() -> new InsufficientBalanceException("Você não possui saldo para esta moeda."));

        if (senderAsset.getBalance().compareTo(request.getQuantity()) < 0) {
            throw new InsufficientBalanceException(String.format("Saldo insuficiente de %s. Saldo atual: %s", request.getSymbol(), senderAsset.getBalance().toPlainString()));
        }

        String destAddress = request.getDestinationWalletAddress().trim();
        CryptoWallet destWallet = cryptoWalletRepository.findByWalletAddress(destAddress)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço de carteira destinatária não encontrado na rede demonstrativa SAP."));

        if (destWallet.getId().equals(senderWallet.getId())) {
            throw new BusinessException("Não é permitido transferir para a própria carteira.");
        }

        CryptoAsset destAsset = cryptoAssetRepository.findByWalletIdAndSymbol(destWallet.getId(), request.getSymbol().name())
                .orElseGet(() -> cryptoAssetRepository.save(new CryptoAsset(destWallet, request.getSymbol().name(), request.getSymbol().getFullName(), BigDecimal.ZERO)));

        // Transfere cripto
        senderAsset.setBalance(senderAsset.getBalance().subtract(request.getQuantity()));
        destAsset.setBalance(destAsset.getBalance().add(request.getQuantity()));

        cryptoAssetRepository.save(senderAsset);
        cryptoAssetRepository.save(destAsset);

        BigDecimal unitPrice = BASE_PRICES.get(request.getSymbol());
        BigDecimal totalBrl = request.getQuantity().multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);

        String txHash = generateTxHash();
        CryptoTransaction tx = new CryptoTransaction(
                txHash,
                senderWallet,
                destWallet,
                request.getSymbol().name(),
                request.getQuantity(),
                unitPrice,
                totalBrl,
                CryptoOperationType.TRANSFERENCIA_P2P
        );
        tx = cryptoTransactionRepository.save(tx);

        notificationService.createNotification(
                senderUserId,
                "Envio de Criptoativo Concluído",
                String.format("Você enviou %s %s para a carteira %s...", request.getQuantity().stripTrailingZeros().toPlainString(), request.getSymbol().name(), destAddress.substring(0, 10)),
                NotificationType.TRANSACTION
        );

        notificationService.createNotification(
                destWallet.getUser().getId(),
                "Recebimento de Criptoativo",
                String.format("Você recebeu %s %s de %s.", request.getQuantity().stripTrailingZeros().toPlainString(), request.getSymbol().name(), senderUser.getFullName()),
                NotificationType.TRANSACTION
        );

        auditService.logAction(senderUserId, senderUser.getEmail(), AuditAction.CRYPTO_TRANSFERRED, "CRYPTO",
                String.format("Transferência P2P de %s %s para %s", request.getQuantity(), request.getSymbol(), destAddress), httpRequest);

        return mapToTransactionResponse(tx);
    }

    private CryptoWallet getOrCreateWallet(Long userId) {
        return cryptoWalletRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
            String address = "0xSAP" + Long.toHexString(userId * 99991L).toUpperCase() + "Fe23Dd" + (1000 + secureRandom.nextInt(9000));
            CryptoWallet wallet = new CryptoWallet(user, address);
            wallet = cryptoWalletRepository.save(wallet);

            for (CryptoSymbol s : CryptoSymbol.values()) {
                cryptoAssetRepository.save(new CryptoAsset(wallet, s.name(), s.getFullName(), BigDecimal.ZERO));
            }
            return wallet;
        });
    }

    private String generateTxHash() {
        StringBuilder sb = new StringBuilder("0x");
        for (int i = 0; i < 64; i++) {
            sb.append(Integer.toHexString(secureRandom.nextInt(16)));
        }
        return sb.toString();
    }

    private void validatePinOrPassword(User user, String pinOrPass) {
        if (pinOrPass == null || pinOrPass.isBlank()) {
            throw new BusinessException("Informe o PIN de segurança para autorizar a operação.");
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

    private CryptoTransactionResponse mapToTransactionResponse(CryptoTransaction tx) {
        return new CryptoTransactionResponse(
                tx.getId(),
                tx.getTxHash(),
                tx.getSymbol(),
                tx.getQuantity(),
                tx.getUnitPriceBrl(),
                tx.getTotalBrl(),
                tx.getOperationType(),
                tx.getSourceWallet() != null ? tx.getSourceWallet().getWalletAddress() : "COMPRA SAP DIRECT",
                tx.getDestinationWallet() != null ? tx.getDestinationWallet().getWalletAddress() : "LIQUIDAÇÃO SAP",
                tx.getStatus(),
                tx.getCreatedAt()
        );
    }
}
