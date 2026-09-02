package com.bancosap.service;

import com.bancosap.dto.request.CreateVirtualCardRequest;
import com.bancosap.dto.request.ToggleCardStatusRequest;
import com.bancosap.dto.response.VirtualCardResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.User;
import com.bancosap.entity.VirtualCard;
import com.bancosap.enums.CardStatus;
import com.bancosap.enums.CardType;
import com.bancosap.enums.RoleName;
import com.bancosap.repository.AccountRepository;
import com.bancosap.repository.TransactionRepository;
import com.bancosap.repository.UserRepository;
import com.bancosap.repository.VirtualCardRepository;
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
class CardServiceTest {

    @Mock
    private VirtualCardRepository virtualCardRepository;
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
    private CardService cardService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = new User("Nicolas Carvalho", "123.456.789-00", LocalDate.of(1998, 3, 25), "(11) 99999-8888", "cliente@bancosap.com.br", "pwd_hash", RoleName.ROLE_CLIENTE);
        user.setId(1L);
        user.setTransactionPinHash("pin_hash");

        account = new Account(user, "33458-1", new BigDecimal("5000.00"));
        account.setId(1L);
    }

    @Test
    @DisplayName("Deve gerar cartão virtual com sucesso")
    void shouldCreateVirtualCardSuccessfully() {
        CreateVirtualCardRequest request = new CreateVirtualCardRequest();
        request.setHolderName("NICOLAS C FERREIRA");
        request.setSpendingLimit(new BigDecimal("2500.00"));
        request.setTemporary(true);
        request.setPin("1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "pin_hash")).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));

        VirtualCard savedCard = new VirtualCard(account, "•••• •••• •••• 9988", "1234567890129988", "NICOLAS C FERREIRA", "09/30", "123", CardType.VIRTUAL, new BigDecimal("2500.00"), true);
        savedCard.setId(10L);
        when(virtualCardRepository.save(any(VirtualCard.class))).thenReturn(savedCard);

        VirtualCardResponse response = cardService.createVirtualCard(1L, request, null);

        assertNotNull(response);
        assertEquals("NICOLAS C FERREIRA", response.getHolderName());
        assertEquals(new BigDecimal("2500.00"), response.getSpendingLimit());
        assertTrue(response.isTemporary());
        verify(virtualCardRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve bloquear e desbloquear cartão com sucesso")
    void shouldToggleCardStatus() {
        VirtualCard card = new VirtualCard(account, "•••• •••• •••• 9988", "1234567890129988", "NICOLAS C FERREIRA", "09/30", "123", CardType.VIRTUAL, new BigDecimal("2500.00"), false);
        card.setId(10L);
        card.setStatus(CardStatus.ATIVO);

        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        when(virtualCardRepository.findByIdAndAccountId(10L, 1L)).thenReturn(Optional.of(card));
        when(virtualCardRepository.save(any())).thenReturn(card);

        ToggleCardStatusRequest request = new ToggleCardStatusRequest();
        request.setCardId(10L);
        request.setStatus(CardStatus.BLOQUEADO);

        VirtualCardResponse response = cardService.toggleCardStatus(1L, request, null);

        assertNotNull(response);
        assertEquals(CardStatus.BLOQUEADO, card.getStatus());
    }
}
