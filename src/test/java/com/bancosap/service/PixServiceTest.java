package com.bancosap.service;

import com.bancosap.dto.request.PixSendRequest;
import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.PixKey;
import com.bancosap.entity.Transaction;
import com.bancosap.entity.User;
import com.bancosap.enums.PixKeyType;
import com.bancosap.enums.RoleName;
import com.bancosap.enums.TransactionType;
import com.bancosap.exception.InsufficientBalanceException;
import com.bancosap.exception.InvalidPixKeyException;
import com.bancosap.repository.AccountRepository;
import com.bancosap.repository.PixKeyRepository;
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
class PixServiceTest {

    @Mock
    private PixKeyRepository pixKeyRepository;
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
    private PixService pixService;

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

        senderAccount = new Account(senderUser, "33458-1", new BigDecimal("2000.00"));
        senderAccount.setId(1L);

        recipientAccount = new Account(recipientUser, "44892-3", new BigDecimal("100.00"));
        recipientAccount.setId(2L);
    }

    @Test
    @DisplayName("Deve enviar PIX com sucesso utilizando chave cadastrada")
    void shouldSendPixSuccessfully() {
        PixSendRequest request = new PixSendRequest();
        request.setKeyValue("maria@bancosap.com.br");
        request.setAmount(new BigDecimal("150.00"));
        request.setPin("1234");

        PixKey key = new PixKey(recipientAccount, PixKeyType.EMAIL, "maria@bancosap.com.br");

        when(userRepository.findById(1L)).thenReturn(Optional.of(senderUser));
        when(passwordEncoder.matches("1234", "pin_hash")).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(senderAccount));
        when(pixKeyRepository.findByKeyValue("maria@bancosap.com.br")).thenReturn(Optional.of(key));

        Transaction tx = new Transaction(senderAccount, recipientAccount, "Maria Silva", "987.654.321-11", "001 - Banco SAP", TransactionType.PIX_ENVIADO, new BigDecimal("150.00"), BigDecimal.ZERO, null, "PIX");
        tx.setId(20L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(tx);

        TransactionResponse response = pixService.sendPix(1L, request, null);

        assertNotNull(response);
        assertEquals(new BigDecimal("150.00"), response.getAmount());
        assertEquals(new BigDecimal("1850.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("250.00"), recipientAccount.getBalance());
    }

    @Test
    @DisplayName("Deve falhar ao enviar PIX para chave inexistente")
    void shouldFailPixWithNonExistentKey() {
        PixSendRequest request = new PixSendRequest();
        request.setKeyValue("chave.fantasma@inexistente.com");
        request.setAmount(new BigDecimal("50.00"));
        request.setPin("1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(senderUser));
        when(passwordEncoder.matches("1234", "pin_hash")).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(senderAccount));
        when(pixKeyRepository.findByKeyValue(any())).thenReturn(Optional.empty());

        assertThrows(InvalidPixKeyException.class, () -> pixService.sendPix(1L, request, null));
    }
}
