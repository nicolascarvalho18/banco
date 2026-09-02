package com.bancosap.service;

import com.bancosap.dto.request.LoginRequest;
import com.bancosap.dto.request.RegisterRequest;
import com.bancosap.dto.response.AuthResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.CryptoWallet;
import com.bancosap.entity.User;
import com.bancosap.enums.RoleName;
import com.bancosap.exception.BusinessException;
import com.bancosap.repository.*;
import com.bancosap.security.JwtTokenProvider;
import com.bancosap.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CryptoWalletRepository cryptoWalletRepository;
    @Mock
    private CryptoAssetRepository cryptoAssetRepository;
    @Mock
    private VirtualCardRepository virtualCardRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private AuditService auditService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Nicolas Carvalho", "nicolas", "123.456.789-00", LocalDate.of(1998, 3, 25), "(11) 99999-8888", "cliente@bancosap.com.br", "hashed_pwd", RoleName.ROLE_CLIENTE);
        sampleUser.setId(1L);
    }

    @Test
    @DisplayName("Deve registrar um novo cliente com sucesso")
    void shouldRegisterNewUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nicolas Carvalho");
        request.setUsername("nicolas");
        request.setCpf("123.456.789-00");
        request.setBirthDate(LocalDate.of(1998, 3, 25));
        request.setPhone("(11) 99999-8888");
        request.setEmail("cliente@bancosap.com.br");
        request.setPassword("BancoSap@2026");
        request.setTermsAccepted(true);

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByCpf(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed_pwd");
        when(userRepository.save(any())).thenReturn(sampleUser);

        Account account = new Account(sampleUser, "33458-1", new BigDecimal("5000.00"));
        account.setId(1L);
        when(accountRepository.save(any())).thenReturn(account);

        CryptoWallet wallet = new CryptoWallet(sampleUser, "0xSAP123");
        wallet.setId(1L);
        when(cryptoWalletRepository.save(any())).thenReturn(wallet);

        when(tokenProvider.generateAccessToken(any())).thenReturn("sample_access_token");
        when(tokenProvider.generateRefreshToken(any())).thenReturn("sample_refresh_token");
        when(tokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.register(request, null);

        assertNotNull(response);
        assertEquals("sample_access_token", response.getAccessToken());
        assertEquals("cliente@bancosap.com.br", response.getUser().getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Deve falhar ao registrar usuário com e-mail duplicado")
    void shouldFailWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicado@bancosap.com.br");
        request.setCpf("123.456.789-00");

        when(userRepository.existsByEmailIgnoreCase("duplicado@bancosap.com.br")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(request, null));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve efetuar login com sucesso")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("cliente@bancosap.com.br", "BancoSap@2026");

        when(loginAttemptService.isIpBlocked(any())).thenReturn(false);
        when(userRepository.findByLoginIdentifier("cliente@bancosap.com.br")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("BancoSap@2026", "hashed_pwd")).thenReturn(true);
        when(tokenProvider.generateAccessToken(any())).thenReturn("sample_access_token");
        when(tokenProvider.generateRefreshToken(any())).thenReturn("sample_refresh_token");

        AuthResponse response = authService.login(request, null);

        assertNotNull(response);
        assertEquals("sample_access_token", response.getAccessToken());
        verify(loginAttemptService, times(1)).loginSucceeded(any(), any());
    }

    @Test
    @DisplayName("Deve rejeitar login com senha incorreta")
    void shouldFailLoginWithBadPassword() {
        LoginRequest request = new LoginRequest("cliente@bancosap.com.br", "SenhaErrada");

        when(loginAttemptService.isIpBlocked(any())).thenReturn(false);
        when(userRepository.findByLoginIdentifier("cliente@bancosap.com.br")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("SenhaErrada", "hashed_pwd")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request, null));
        verify(loginAttemptService, times(1)).loginFailed(any(), any());
    }
}
