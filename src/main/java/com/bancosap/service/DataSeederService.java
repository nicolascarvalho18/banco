package com.bancosap.service;

import com.bancosap.entity.*;
import com.bancosap.enums.*;
import com.bancosap.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class DataSeederService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederService.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PixKeyRepository pixKeyRepository;
    private final VirtualCardRepository virtualCardRepository;
    private final CryptoWalletRepository cryptoWalletRepository;
    private final CryptoAssetRepository cryptoAssetRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeederService(UserRepository userRepository, AccountRepository accountRepository,
                             PixKeyRepository pixKeyRepository, VirtualCardRepository virtualCardRepository,
                             CryptoWalletRepository cryptoWalletRepository, CryptoAssetRepository cryptoAssetRepository,
                             TransactionRepository transactionRepository, NotificationRepository notificationRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.pixKeyRepository = pixKeyRepository;
        this.virtualCardRepository = virtualCardRepository;
        this.cryptoWalletRepository = cryptoWalletRepository;
        this.cryptoAssetRepository = cryptoAssetRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Banco SAF: Dados já inicializados no banco.");
            return;
        }

        log.info("Banco SAF: Inicializando dados do sistema...");

        String defaultPassHash = passwordEncoder.encode("BancoSap@2026");
        String adminPassHash = passwordEncoder.encode("Admin@2026");
        String pinHash = passwordEncoder.encode("1234");

        // 1. Administrador
        User admin = new User("Administrador do Sistema", "000.000.000-00", LocalDate.of(1988, 5, 15), "(11) 98888-0000", "admin@bancosap.com.br", adminPassHash, RoleName.ROLE_ADMIN);
        admin.setUsername("admin");
        admin.setTransactionPinHash(pinHash);
        admin.setAddress("Av. Paulista, 1000 - Bela Vista, São Paulo/SP");
        admin = userRepository.save(admin);

        Account adminAcc = new Account(admin, "10001-9", new BigDecimal("500000.00"));
        adminAcc.setCreditLimit(new BigDecimal("100000.00"));
        adminAcc = accountRepository.save(adminAcc);

        // 2. Atendente / Operador
        User op = new User("Atendente Operacional SAF", "111.111.111-11", LocalDate.of(1995, 10, 20), "(11) 97777-1111", "operador@bancosap.com.br", defaultPassHash, RoleName.ROLE_ATENDENTE);
        op.setUsername("operador");
        op.setTransactionPinHash(pinHash);
        op.setAddress("Av. Brigadeiro Faria Lima, 2000 - Itaim Bibi, São Paulo/SP");
        op = userRepository.save(op);

        Account opAcc = new Account(op, "20002-8", new BigDecimal("25000.00"));
        opAcc.setSavingsBalance(new BigDecimal("5000.00"));
        opAcc = accountRepository.save(opAcc);

        // 3. Cliente Principal (Nicolas Carvalho Ferreira)
        User client1 = new User("Nicolas Carvalho Ferreira", "123.456.789-00", LocalDate.of(1998, 3, 25), "(11) 99999-8888", "cliente@bancosap.com.br", defaultPassHash, RoleName.ROLE_CLIENTE);
        client1.setUsername("nicolas");
        client1.setTransactionPinHash(pinHash);
        client1.setAddress("Rua Oscar Freire, 850 - Jardins, São Paulo/SP");
        client1 = userRepository.save(client1);

        Account client1Acc = new Account(client1, "33458-1", new BigDecimal("14850.75"));
        client1Acc.setSavingsBalance(new BigDecimal("4200.00"));
        client1Acc.setCreditLimit(new BigDecimal("12000.00"));
        client1Acc.setDailyPixLimit(new BigDecimal("15000.00"));
        client1Acc = accountRepository.save(client1Acc);

        // Chaves PIX Cliente 1
        pixKeyRepository.save(new PixKey(client1Acc, PixKeyType.CPF, "123.456.789-00"));
        pixKeyRepository.save(new PixKey(client1Acc, PixKeyType.EMAIL, "cliente@bancosap.com.br"));
        pixKeyRepository.save(new PixKey(client1Acc, PixKeyType.ALEATORIA, "9b8e21a4-7c33-4f90-891d-28e34fc901aa"));

        // Cartões Cliente 1
        VirtualCard card1 = new VirtualCard(client1Acc, "•••• •••• •••• 8842", "5544882211998842", "NICOLAS C FERREIRA", "09/30", "784", CardType.FISICO, new BigDecimal("12000.00"), false);
        card1.setUsedLimit(new BigDecimal("1420.50"));
        virtualCardRepository.save(card1);

        // Carteira Cripto Cliente 1 com Preço Médio Realista
        CryptoWallet wallet1 = new CryptoWallet(client1, "0xSAF77a9b8C41Fe23Dd091F8301B6d4f9A02e5C81");
        wallet1 = cryptoWalletRepository.save(wallet1);

        cryptoAssetRepository.save(new CryptoAsset(wallet1, "BTC", "Bitcoin", new BigDecimal("0.12500000"), new BigDecimal("350000.00")));
        cryptoAssetRepository.save(new CryptoAsset(wallet1, "ETH", "Ethereum", new BigDecimal("1.85000000"), new BigDecimal("15500.00")));
        cryptoAssetRepository.save(new CryptoAsset(wallet1, "SOL", "Solana", new BigDecimal("12.40000000"), new BigDecimal("620.00")));
        cryptoAssetRepository.save(new CryptoAsset(wallet1, "USDT", "Tether USD", new BigDecimal("850.00000000"), new BigDecimal("5.45")));
        cryptoAssetRepository.save(new CryptoAsset(wallet1, "ADA", "Cardano", new BigDecimal("450.00000000"), new BigDecimal("1.95")));

        // 4. Cliente Secundária (Maria Helena Silva)
        User client2 = new User("Maria Helena Silva", "987.654.321-11", LocalDate.of(1992, 7, 14), "(21) 98888-7777", "maria.silva@bancosap.com.br", defaultPassHash, RoleName.ROLE_CLIENTE);
        client2.setUsername("mariasilva");
        client2.setTransactionPinHash(pinHash);
        client2.setAddress("Av. Atlântica, 450 - Copacabana, Rio de Janeiro/RJ");
        client2 = userRepository.save(client2);

        Account client2Acc = new Account(client2, "44892-3", new BigDecimal("8320.50"));
        client2Acc.setSavingsBalance(new BigDecimal("1500.00"));
        client2Acc = accountRepository.save(client2Acc);

        CryptoWallet wallet2 = new CryptoWallet(client2, "0xSAF11f23E90bB54Aa329C011F492E7d1B55a7F99");
        wallet2 = cryptoWalletRepository.save(wallet2);
        cryptoAssetRepository.save(new CryptoAsset(wallet2, "BTC", "Bitcoin", new BigDecimal("0.05000000"), new BigDecimal("345000.00")));
        cryptoAssetRepository.save(new CryptoAsset(wallet2, "ETH", "Ethereum", new BigDecimal("0.50000000"), new BigDecimal("15200.00")));
        cryptoAssetRepository.save(new CryptoAsset(wallet2, "USDT", "Tether USD", new BigDecimal("300.00000000"), new BigDecimal("5.45")));

        // Transações de Exemplo
        Transaction t1 = new Transaction(null, client1Acc, "Nicolas Carvalho", client1.getCpf(), "Banco SAF", TransactionType.DEPOSITO, new BigDecimal("10000.00"), BigDecimal.ZERO, TransactionCategory.INVESTIMENTOS, "Depósito inicial de boas-vindas");
        transactionRepository.save(t1);

        // Notificações Iniciais
        notificationRepository.save(new Notification(client1, "Bem-vindo ao Banco SAF!", "Sua carteira de custódia e conta digital estão ativas.", NotificationType.SUCCESS));

        log.info("Banco SAF: Dados do sistema carregados com sucesso!");
    }
}
