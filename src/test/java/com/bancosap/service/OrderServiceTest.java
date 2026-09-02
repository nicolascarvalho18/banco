package com.bancosap.service;

import com.bancosap.dto.request.SimulatedBuyRequest;
import com.bancosap.dto.request.SimulatedSellRequest;
import com.bancosap.dto.response.SimulatedOrderResponse;
import com.bancosap.entity.*;
import com.bancosap.enums.RoleName;
import com.bancosap.exception.InsufficientBalanceException;
import com.bancosap.exception.UnauthorizedException;
import com.bancosap.market.MarketDataService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private SimulatedOrderRepository orderRepository;
    @Mock private LedgerEntryRepository ledgerRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CryptoWalletRepository walletRepository;
    @Mock private CryptoAssetRepository assetRepository;
    @Mock private UserRepository userRepository;
    @Mock private MarketDataService marketDataService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Account testAccount;
    private CryptoWallet testWallet;

    @BeforeEach
    void setUp() {
        testUser = new User("Nicolas C Ferreira", "nicolas", "123.456.789-00", LocalDate.of(1998, 3, 25), "(11) 99999-8888", "cliente@bancosap.com.br", "hashed_pass", RoleName.ROLE_CLIENTE);
        testUser.setId(1L);
        testUser.setTransactionPinHash("hashed_pin");

        testAccount = new Account(testUser, "33458-1", new BigDecimal("10000.00"));
        testAccount.setId(1L);

        testWallet = new CryptoWallet(testUser, "0xSAP1234567890ABCDEF");
        testWallet.setId(1L);
    }

    @Test
    @DisplayName("Deve executar compra simulada de BTC com sucesso e registrar no ledger de dupla entrada")
    void executeBuy_Success() {
        SimulatedBuyRequest request = new SimulatedBuyRequest("BTC", new BigDecimal("1000.00"), "1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("1234", "hashed_pin")).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(testAccount));
        when(marketDataService.getPriceInBrl("BTC")).thenReturn(new BigDecimal("350000.00"));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
        when(assetRepository.findByWalletIdAndSymbol(1L, "BTC")).thenReturn(Optional.empty());
        when(orderRepository.save(any(SimulatedOrder.class))).thenAnswer(i -> i.getArgument(0));

        SimulatedOrderResponse response = orderService.executeBuy(1L, request, "127.0.0.1");

        assertNotNull(response);
        assertEquals("COMPRA", response.getOrderType());
        assertEquals("BTC", response.getSymbolTo());
        assertEquals(new BigDecimal("1000.00"), response.getAmountFrom());

        // Verificar débito do saldo da conta
        assertEquals(new BigDecimal("9000.00"), testAccount.getBalance());

        // Verificar registro de 2 entradas no ledger (Débito BRL e Crédito BTC)
        verify(ledgerRepository, times(2)).save(any(LedgerEntry.class));
    }

    @Test
    @DisplayName("Deve lançar InsufficientBalanceException ao tentar comprar sem saldo BRL suficiente")
    void executeBuy_InsufficientFunds() {
        testAccount.setBalance(new BigDecimal("50.00"));
        SimulatedBuyRequest request = new SimulatedBuyRequest("BTC", new BigDecimal("500.00"), "1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("1234", "hashed_pin")).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(InsufficientBalanceException.class, () -> orderService.executeBuy(1L, request, "127.0.0.1"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException com PIN incorreto")
    void executeBuy_InvalidPin() {
        SimulatedBuyRequest request = new SimulatedBuyRequest("BTC", new BigDecimal("100.00"), "9999");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("9999", "hashed_pin")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> orderService.executeBuy(1L, request, "127.0.0.1"));
    }
}
