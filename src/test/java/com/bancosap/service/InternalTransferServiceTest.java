package com.bancosap.service;

import com.bancosap.dto.request.InternalTransferRequest;
import com.bancosap.dto.response.InternalTransferResponse;
import com.bancosap.entity.*;
import com.bancosap.enums.RoleName;
import com.bancosap.exception.BusinessException;
import com.bancosap.exception.InsufficientBalanceException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalTransferServiceTest {

    @Mock private InternalTransferRepository transferRepository;
    @Mock private LedgerEntryRepository ledgerRepository;
    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CryptoWalletRepository walletRepository;
    @Mock private CryptoAssetRepository assetRepository;
    @Mock private MarketDataService marketDataService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;

    @InjectMocks
    private InternalTransferService transferService;

    private User sender;
    private User recipient;
    private Account senderAcc;
    private Account recipientAcc;

    @BeforeEach
    void setUp() {
        sender = new User("Nicolas Ferreira", "nicolas", "123.456.789-00", LocalDate.of(1998, 3, 25), "(11) 99999-8888", "cliente@bancosap.com.br", "pass", RoleName.ROLE_CLIENTE);
        sender.setId(1L);
        sender.setTransactionPinHash("hashed_pin");

        recipient = new User("Maria Silva", "mariasilva", "987.654.321-11", LocalDate.of(1992, 7, 14), "(21) 98888-7777", "maria@bancosap.com.br", "pass", RoleName.ROLE_CLIENTE);
        recipient.setId(2L);

        senderAcc = new Account(sender, "11111-1", new BigDecimal("5000.00"));
        senderAcc.setId(1L);

        recipientAcc = new Account(recipient, "22222-2", new BigDecimal("1000.00"));
        recipientAcc.setId(2L);
    }

    @Test
    @DisplayName("Deve realizar transferência interna em BRL por username com sucesso")
    void executeTransfer_Brl_Success() {
        InternalTransferRequest request = new InternalTransferRequest("mariasilva", "BRL", new BigDecimal("500.00"), "Divisão de despesas", "1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(passwordEncoder.matches("1234", "hashed_pin")).thenReturn(true);
        when(userRepository.findByLoginIdentifier("mariasilva")).thenReturn(Optional.of(recipient));
        when(marketDataService.getPriceInBrl("BRL")).thenReturn(BigDecimal.ONE);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(senderAcc));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(recipientAcc));
        when(transferRepository.save(any(InternalTransfer.class))).thenAnswer(i -> i.getArgument(0));

        InternalTransferResponse response = transferService.executeTransfer(1L, request, "127.0.0.1");

        assertNotNull(response);
        assertEquals("BRL", response.getSymbol());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals(new BigDecimal("4500.00"), senderAcc.getBalance());
        assertEquals(new BigDecimal("1500.00"), recipientAcc.getBalance());

        // Verificar 2 lançamentos no ledger
        verify(ledgerRepository, times(2)).save(any(LedgerEntry.class));
    }

    @Test
    @DisplayName("Deve proibir transferência para a própria conta")
    void executeTransfer_SelfTransfer_Forbidden() {
        InternalTransferRequest request = new InternalTransferRequest("nicolas", "BRL", new BigDecimal("100.00"), "Auto envio", "1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(passwordEncoder.matches("1234", "hashed_pin")).thenReturn(true);
        when(userRepository.findByLoginIdentifier("nicolas")).thenReturn(Optional.of(sender));

        assertThrows(BusinessException.class, () -> transferService.executeTransfer(1L, request, "127.0.0.1"));
    }
}
