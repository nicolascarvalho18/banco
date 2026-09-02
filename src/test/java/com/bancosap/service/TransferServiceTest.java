package com.bancosap.service;

import com.bancosap.dto.request.TransferRequest;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.Transaction;
import com.bancosap.entity.User;
import com.bancosap.enums.RoleName;
import com.bancosap.enums.TransactionType;
import com.bancosap.exception.BusinessException;
import com.bancosap.exception.InsufficientBalanceException;
import com.bancosap.repository.AccountRepository;
import com.bancosap.repository.TransactionRepository;
import com.bancosap.repository.UserRepository;
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
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransferService transferService;

    private User senderUser;
    private User recipientUser;
    private Account senderAccount;
    private Account recipientAccount;

    @BeforeEach
    void setUp() {
        senderUser = new User("Nicolas Carvalho", "123.456.789-00", LocalDate.of(1998, 3, 25), "(11) 99999-8888", "cliente@bancosap.com.br", "pwd_hash", RoleName.ROLE_CLIENTE);
        senderUser.setId(1L);
        senderUser.setTransactionPinHash("pin_hash");

        recipientUser = new User("Maria Silva", "987.654.321-11", LocalDate.of(1992, 7, 14), "(21) 98888-7777", "maria@bancosap.com.br", "pwd_hash", RoleName.ROLE_CLIENTE);
        recipientUser.setId(2L);

        senderAccount = new Account(senderUser, "33458-1", new BigDecimal("1000.00"));
        senderAccount.setId(1L);

        recipientAccount = new Account(recipientUser, "44892-3", new BigDecimal("500.00"));
        recipientAccount.setId(2L);
    }

    @Test
    @DisplayName("Deve executar transferência entre contas com saldo suficiente")
    void shouldExecuteTransferSuccessfully() {
        TransferRequest request = new TransferRequest();
        request.setDestinationIdentifier("maria@bancosap.com.br");
        request.setAmount(new BigDecimal("300.00"));
        request.setPin("1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(senderUser));
        when(passwordEncoder.matches("1234", "pin_hash")).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserEmail("maria@bancosap.com.br")).thenReturn(Optional.of(recipientAccount));

        Transaction savedTx = new Transaction(senderAccount, recipientAccount, "Maria Silva", "987.654.321-11", "001 - Banco SAP", TransactionType.TRANSFERENCIA_ENVIADA, new BigDecimal("300.00"), BigDecimal.ZERO, null, "Transferência");
        savedTx.setId(10L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transferService.executeTransfer(1L, request, null);

        assertNotNull(response);
        assertEquals(new BigDecimal("300.00"), response.getAmount());
        assertEquals(new BigDecimal("700.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("800.00"), recipientAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("Deve rejeitar transferência quando saldo é insuficiente")
    void shouldRejectTransferWhenInsufficientBalance() {
        TransferRequest request = new TransferRequest();
        request.setDestinationIdentifier("maria@bancosap.com.br");
        request.setAmount(new BigDecimal("1500.00")); // Saldo é 1000
        request.setPin("1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(senderUser));
        when(passwordEncoder.matches("1234", "pin_hash")).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(senderAccount));

        assertThrows(InsufficientBalanceException.class, () -> transferService.executeTransfer(1L, request, null));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar transferência para a própria conta")
    void shouldRejectTransferToSelf() {
        TransferRequest request = new TransferRequest();
        request.setDestinationIdentifier("cliente@bancosap.com.br");
        request.setAmount(new BigDecimal("100.00"));
        request.setPin("1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(senderUser));
        when(passwordEncoder.matches("1234", "pin_hash")).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserEmail("cliente@bancosap.com.br")).thenReturn(Optional.of(senderAccount));

        assertThrows(BusinessException.class, () -> transferService.executeTransfer(1L, request, null));
    }
}
