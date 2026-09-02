package com.bancosap.service;

import com.bancosap.dto.request.CryptoTradeRequest;
import com.bancosap.dto.response.CryptoQuoteResponse;
import com.bancosap.dto.response.CryptoTransactionResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.CryptoAsset;
import com.bancosap.entity.CryptoWallet;
import com.bancosap.entity.User;
import com.bancosap.enums.CryptoOperationType;
import com.bancosap.enums.CryptoSymbol;
import com.bancosap.enums.RoleName;
import com.bancosap.exception.InsufficientBalanceException;
import com.bancosap.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    @Mock
    private CryptoWalletRepository cryptoWalletRepository;
    @Mock
    private CryptoAssetRepository cryptoAssetRepository;
    @Mock
    private CryptoTransactionRepository cryptoTransactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private CryptoService cryptoService;

    private User user;
    private Account account;
    private CryptoWallet wallet;

    @BeforeEach
    void setUp() {
        user = new User("Nicolas Carvalho", "123.456.789-00", LocalDate.of(1998, 3, 25), "(11) 99999-8888", "cliente@bancosap.com.br", "pwd_hash", RoleName.ROLE_CLIENTE);
        user.setId(1L);
        user.setTransactionPinHash("pin_hash");

        account = new Account(user, "33458-1", new BigDecimal("5000.00"));
        account.setId(1L);

        wallet = new CryptoWallet(user, "0xSAP77a9b8C41Fe23Dd091F8301B6d4f9A02e5C81");
        wallet.setId(1L);
    }

    @Test
    @DisplayName("Deve retornar cotações simuladas para todas as moedas suportadas")
    void shouldReturnMarketQuotes() {
        List<CryptoQuoteResponse> quotes = cryptoService.getMarketQuotes();
        assertNotNull(quotes);
        assertEquals(5, quotes.size());
        assertTrue(quotes.stream().anyMatch(q -> q.getSymbol().equals("BTC")));
        assertTrue(quotes.stream().anyMatch(q -> q.getSymbol().equals("ETH")));
    }

    @Test
    @DisplayName("Deve comprar criptoativo deduzindo saldo em reais")
    void shouldBuyCryptoSuccessfully() {
        CryptoTradeRequest request = new CryptoTradeRequest();
        request.setSymbol(CryptoSymbol.BTC);
        request.setOperationType(CryptoOperationType.COMPRA);
        request.setAmountBrl(new BigDecimal("1000.00"));
        request.setPin("1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "pin_hash")).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        when(cryptoWalletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        CryptoAsset btcAsset = new CryptoAsset(wallet, "BTC", "Bitcoin", BigDecimal.ZERO);
        when(cryptoAssetRepository.findByWalletIdAndSymbol(1L, "BTC")).thenReturn(Optional.of(btcAsset));
        when(cryptoTransactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CryptoTransactionResponse response = cryptoService.trade(1L, request, null);

        assertNotNull(response);
        assertEquals("BTC", response.getSymbol());
        assertEquals(CryptoOperationType.COMPRA, response.getOperationType());
        assertEquals(new BigDecimal("4000.00"), account.getBalance()); // 5000 - 1000
        assertTrue(btcAsset.getBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Deve falhar compra de cripto quando saldo em reais é insuficiente")
    void shouldFailBuyWhenInsufficientBrlBalance() {
        CryptoTradeRequest request = new CryptoTradeRequest();
        request.setSymbol(CryptoSymbol.BTC);
        request.setOperationType(CryptoOperationType.COMPRA);
        request.setAmountBrl(new BigDecimal("10000.00")); // Saldo é 5000
        request.setPin("1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "pin_hash")).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        when(cryptoWalletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(cryptoAssetRepository.findByWalletIdAndSymbol(1L, "BTC")).thenReturn(Optional.of(new CryptoAsset(wallet, "BTC", "Bitcoin", BigDecimal.ZERO)));

        assertThrows(InsufficientBalanceException.class, () -> cryptoService.trade(1L, request, null));
    }
}
