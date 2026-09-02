package com.bancosap.service;

import com.bancosap.dto.request.SimulatedBuyRequest;
import com.bancosap.dto.request.SimulatedConvertRequest;
import com.bancosap.dto.request.SimulatedSellRequest;
import com.bancosap.dto.response.SimulatedOrderResponse;
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
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final SimulatedOrderRepository orderRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final AccountRepository accountRepository;
    private final CryptoWalletRepository walletRepository;
    private final CryptoAssetRepository assetRepository;
    private final UserRepository userRepository;
    private final MarketDataService marketDataService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditService auditService;

    private static final BigDecimal SIMULATED_FEE_PERCENT = new BigDecimal("0.0015"); // 0.15% taxa

    public OrderService(SimulatedOrderRepository orderRepository,
                        LedgerEntryRepository ledgerRepository,
                        AccountRepository accountRepository,
                        CryptoWalletRepository walletRepository,
                        CryptoAssetRepository assetRepository,
                        UserRepository userRepository,
                        MarketDataService marketDataService,
                        PasswordEncoder passwordEncoder,
                        NotificationService notificationService,
                        AuditService auditService) {
        this.orderRepository = orderRepository;
        this.ledgerRepository = ledgerRepository;
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.marketDataService = marketDataService;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * COMPRA SIMULADA: Debita BRL e Credita Criptoativo com cotação real atual
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SimulatedOrderResponse executeBuy(Long userId, SimulatedBuyRequest request, String ipAddress) {
        User user = validateUserAndPin(userId, request.getPin());

        // Idempotency check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<SimulatedOrder> existing = orderRepository.findByIdempotencyKeyAndUserId(request.getIdempotencyKey(), userId);
            if (existing.isPresent()) {
                return toDto(existing.get());
            }
        }

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta de saldo BRL não encontrada."));

        BigDecimal amountBrl = request.getAmountBrl();
        if (account.getBalance().compareTo(amountBrl) < 0) {
            throw new InsufficientBalanceException("Saldo em reais insuficiente para realizar a compra.");
        }

        BigDecimal unitPriceBrl = marketDataService.getPriceInBrl(request.getSymbol());
        BigDecimal feeBrl = amountBrl.multiply(SIMULATED_FEE_PERCENT).setScale(4, RoundingMode.HALF_UP);
        BigDecimal netBrl = amountBrl.subtract(feeBrl);
        BigDecimal cryptoAmountAcquired = netBrl.divide(unitPriceBrl, 8, RoundingMode.HALF_DOWN);

        // 1. Debitar saldo em Reais
        account.setBalance(account.getBalance().subtract(amountBrl));
        accountRepository.save(account);

        // 2. Creditar na carteira de Criptoativos
        CryptoWallet wallet = getOrCreateWallet(user);
        CryptoAsset asset = assetRepository.findByWalletIdAndSymbol(wallet.getId(), request.getSymbol().toUpperCase())
                .orElseGet(() -> new CryptoAsset(wallet, request.getSymbol().toUpperCase(), request.getSymbol().toUpperCase(), BigDecimal.ZERO));

        // Atualizar preço médio de aquisição
        BigDecimal currentTotalCost = asset.getBalance().multiply(asset.getAveragePurchasePrice());
        BigDecimal newTotalBalance = asset.getBalance().add(cryptoAmountAcquired);
        BigDecimal newAveragePrice = (newTotalBalance.compareTo(BigDecimal.ZERO) > 0)
                ? currentTotalCost.add(netBrl).divide(newTotalBalance, 4, RoundingMode.HALF_UP)
                : unitPriceBrl;

        asset.setBalance(newTotalBalance);
        asset.setAveragePurchasePrice(newAveragePrice);
        assetRepository.save(asset);

        // 3. Registrar Ordem
        SimulatedOrder order = new SimulatedOrder(
                user,
                "COMPRA",
                "BRL",
                request.getSymbol().toUpperCase(),
                amountBrl,
                cryptoAmountAcquired,
                unitPriceBrl,
                feeBrl,
                request.getIdempotencyKey()
        );
        SimulatedOrder savedOrder = orderRepository.save(order);

        // 4. Registrar Livro Razão de Dupla Entrada (Ledger)
        ledgerRepository.save(new LedgerEntry(
                savedOrder.getAuthenticationCode(),
                user,
                "DEBITO",
                "BRL",
                amountBrl,
                account.getBalance(),
                "Compra de " + cryptoAmountAcquired + " " + request.getSymbol().toUpperCase()
        ));

        ledgerRepository.save(new LedgerEntry(
                savedOrder.getAuthenticationCode(),
                user,
                "CREDITO",
                request.getSymbol().toUpperCase(),
                cryptoAmountAcquired,
                asset.getBalance(),
                "Aquisição por R$ " + amountBrl
        ));

        notificationService.createNotification(user.getId(), "Ordem de Compra Executada",
                "Você comprou " + cryptoAmountAcquired + " " + request.getSymbol().toUpperCase() + " por R$ " + amountBrl,
                NotificationType.SUCCESS);

        auditService.logAction(userId, user.getEmail(), AuditAction.CRYPTO_BOUGHT, "simulated_orders", "Compra " + request.getSymbol(), null);

        return toDto(savedOrder);
    }

    /**
     * VENDA SIMULADA: Debita Criptoativo e Credita BRL com cotação real atual
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SimulatedOrderResponse executeSell(Long userId, SimulatedSellRequest request, String ipAddress) {
        User user = validateUserAndPin(userId, request.getPin());

        CryptoWallet wallet = getOrCreateWallet(user);
        CryptoAsset asset = assetRepository.findByWalletIdAndSymbol(wallet.getId(), request.getSymbol().toUpperCase())
                .orElseThrow(() -> new BusinessException("Você não possui saldo deste criptoativo para venda."));

        BigDecimal cryptoToSell = request.getCryptoAmount();
        if (asset.getBalance().compareTo(cryptoToSell) < 0) {
            throw new InsufficientBalanceException("Saldo de " + request.getSymbol() + " insuficiente para a venda.");
        }

        BigDecimal unitPriceBrl = marketDataService.getPriceInBrl(request.getSymbol());
        BigDecimal grossBrl = cryptoToSell.multiply(unitPriceBrl).setScale(2, RoundingMode.HALF_DOWN);
        BigDecimal feeBrl = grossBrl.multiply(SIMULATED_FEE_PERCENT).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netBrl = grossBrl.subtract(feeBrl);

        // 1. Debitar saldo de cripto
        asset.setBalance(asset.getBalance().subtract(cryptoToSell));
        assetRepository.save(asset);

        // 2. Creditar saldo em reais
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta de saldo BRL não encontrada."));
        account.setBalance(account.getBalance().add(netBrl));
        accountRepository.save(account);

        // 3. Registrar Ordem
        SimulatedOrder order = new SimulatedOrder(
                user,
                "VENDA",
                request.getSymbol().toUpperCase(),
                "BRL",
                cryptoToSell,
                netBrl,
                unitPriceBrl,
                feeBrl,
                request.getIdempotencyKey()
        );
        SimulatedOrder savedOrder = orderRepository.save(order);

        // 4. Ledger Dupla Entrada
        ledgerRepository.save(new LedgerEntry(
                savedOrder.getAuthenticationCode(),
                user,
                "DEBITO",
                request.getSymbol().toUpperCase(),
                cryptoToSell,
                asset.getBalance(),
                "Venda de " + cryptoToSell + " " + request.getSymbol().toUpperCase()
        ));

        ledgerRepository.save(new LedgerEntry(
                savedOrder.getAuthenticationCode(),
                user,
                "CREDITO",
                "BRL",
                netBrl,
                account.getBalance(),
                "Liquidação de venda em Reais"
        ));

        notificationService.createNotification(user.getId(), "Ordem de Venda Executada",
                "Você vendeu " + cryptoToSell + " " + request.getSymbol().toUpperCase() + " recebendo R$ " + netBrl,
                NotificationType.SUCCESS);

        auditService.logAction(userId, user.getEmail(), AuditAction.CRYPTO_SOLD, "simulated_orders", "Venda " + request.getSymbol(), null);

        return toDto(savedOrder);
    }

    /**
     * CONVERSÃO DIRETA ENTRE CRIPTOMOEDAS (ex: BTC -> ETH)
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SimulatedOrderResponse executeConvert(Long userId, SimulatedConvertRequest request, String ipAddress) {
        User user = validateUserAndPin(userId, request.getPin());

        String from = request.getFromSymbol().toUpperCase();
        String to = request.getToSymbol().toUpperCase();

        if (from.equals(to)) {
            throw new BusinessException("A moeda de origem e destino devem ser diferentes.");
        }

        CryptoWallet wallet = getOrCreateWallet(user);
        CryptoAsset fromAsset = assetRepository.findByWalletIdAndSymbol(wallet.getId(), from)
                .orElseThrow(() -> new BusinessException("Você não possui saldo de " + from + " para conversão."));

        BigDecimal fromAmount = request.getFromAmount();
        if (fromAsset.getBalance().compareTo(fromAmount) < 0) {
            throw new InsufficientBalanceException("Saldo de " + from + " insuficiente para a conversão.");
        }

        BigDecimal priceFromBrl = marketDataService.getPriceInBrl(from);
        BigDecimal priceToBrl = marketDataService.getPriceInBrl(to);

        BigDecimal grossBrlValue = fromAmount.multiply(priceFromBrl);
        BigDecimal feeBrl = grossBrlValue.multiply(SIMULATED_FEE_PERCENT).setScale(4, RoundingMode.HALF_UP);
        BigDecimal netBrlValue = grossBrlValue.subtract(feeBrl);

        BigDecimal toAmount = netBrlValue.divide(priceToBrl, 8, RoundingMode.HALF_DOWN);

        // 1. Debitar da moeda de origem
        fromAsset.setBalance(fromAsset.getBalance().subtract(fromAmount));
        assetRepository.save(fromAsset);

        // 2. Creditar na moeda de destino
        CryptoAsset toAsset = assetRepository.findByWalletIdAndSymbol(wallet.getId(), to)
                .orElseGet(() -> new CryptoAsset(wallet, to, to, BigDecimal.ZERO));
        toAsset.setBalance(toAsset.getBalance().add(toAmount));
        toAsset.setAveragePurchasePrice(priceToBrl);
        assetRepository.save(toAsset);

        // 3. Ordem
        SimulatedOrder order = new SimulatedOrder(
                user,
                "CONVERSAO",
                from,
                to,
                fromAmount,
                toAmount,
                priceFromBrl,
                feeBrl,
                request.getIdempotencyKey()
        );
        SimulatedOrder savedOrder = orderRepository.save(order);

        // 4. Ledger
        ledgerRepository.save(new LedgerEntry(
                savedOrder.getAuthenticationCode(),
                user,
                "DEBITO",
                from,
                fromAmount,
                fromAsset.getBalance(),
                "Conversão de " + from + " para " + to
        ));

        ledgerRepository.save(new LedgerEntry(
                savedOrder.getAuthenticationCode(),
                user,
                "CREDITO",
                to,
                toAmount,
                toAsset.getBalance(),
                "Conversão recebida de " + from
        ));

        notificationService.createNotification(user.getId(), "Conversão Concluída",
                "Você converteu " + fromAmount + " " + from + " em " + toAmount + " " + to,
                NotificationType.SUCCESS);

        auditService.logAction(userId, user.getEmail(), AuditAction.CRYPTO_TRANSFERRED, "simulated_orders", "Conversão " + from + "->" + to, null);

        return toDto(savedOrder);
    }

    private User validateUserAndPin(Long userId, String pin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (user.getTransactionPinHash() == null) {
            throw new UnauthorizedException("Configure seu PIN de segurança nas configurações de perfil antes de operar.");
        }

        if (!passwordEncoder.matches(pin, user.getTransactionPinHash())) {
            throw new UnauthorizedException("PIN de segurança incorreto.");
        }

        return user;
    }

    private CryptoWallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    CryptoWallet w = new CryptoWallet(user, "0xSAP" + UUID.randomUUID().toString().replace("-", "").substring(0, 34).toUpperCase());
                    return walletRepository.save(w);
                });
    }

    private SimulatedOrderResponse toDto(SimulatedOrder o) {
        return new SimulatedOrderResponse(
                o.getAuthenticationCode(),
                o.getOrderType(),
                o.getSymbolFrom(),
                o.getSymbolTo(),
                o.getAmountFrom(),
                o.getAmountTo(),
                o.getUnitPriceBrl(),
                o.getFeeBrl(),
                o.getStatus(),
                o.getCreatedAt()
        );
    }
}
