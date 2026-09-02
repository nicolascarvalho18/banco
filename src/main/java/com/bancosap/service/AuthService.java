package com.bancosap.service;

import com.bancosap.dto.request.*;
import com.bancosap.dto.response.AuthResponse;
import com.bancosap.dto.response.UserSummaryResponse;
import com.bancosap.entity.*;
import com.bancosap.enums.*;
import com.bancosap.exception.*;
import com.bancosap.repository.*;
import com.bancosap.security.JwtTokenProvider;
import com.bancosap.security.LoginAttemptService;
import com.bancosap.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CryptoWalletRepository cryptoWalletRepository;
    private final CryptoAssetRepository cryptoAssetRepository;
    private final VirtualCardRepository virtualCardRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository, AccountRepository accountRepository,
                       CryptoWalletRepository cryptoWalletRepository, CryptoAssetRepository cryptoAssetRepository,
                       VirtualCardRepository virtualCardRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider, LoginAttemptService loginAttemptService,
                       AuditService auditService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.cryptoWalletRepository = cryptoWalletRepository;
        this.cryptoAssetRepository = cryptoAssetRepository;
        this.virtualCardRepository = virtualCardRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.loginAttemptService = loginAttemptService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        String cleanCpf = request.getCpf().replaceAll("\\D", "");
        String formattedCpf = formatCpf(cleanCpf);

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("Já existe um cadastro com este e-mail.");
        }
        if (request.getUsername() != null && userRepository.existsByUsernameIgnoreCase(request.getUsername().trim())) {
            throw new BusinessException("Este nome de usuário já está em uso.");
        }
        if (userRepository.existsByCpf(formattedCpf) || userRepository.existsByCpf(cleanCpf)) {
            throw new BusinessException("Já existe um cadastro com este CPF.");
        }

        // Criação do usuário
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setUsername(request.getUsername() != null ? request.getUsername().toLowerCase().trim() : null);
        user.setCpf(formattedCpf);
        user.setBirthDate(request.getBirthDate());
        user.setPhone(request.getPhone().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleName.ROLE_CLIENTE);
        user.setStatus(UserStatus.ATIVO);
        user = userRepository.save(user);

        // Criação da Conta Bancária com Saldo Inicial Simulado
        String accountNumber = generateAccountNumber();
        Account account = new Account();
        account.setUser(user);
        account.setAgencyNumber("0001");
        account.setAccountNumber(accountNumber);
        account.setAccountType(AccountType.CORRENTE);
        account.setBalance(new BigDecimal("10000.00")); // R$ 10.000,00 simulados
        account.setSavingsBalance(new BigDecimal("2000.00"));
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setDailyPixLimit(new BigDecimal("10000.00"));
        account.setNightlyPixLimit(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.ATIVO);
        account = accountRepository.save(account);

        // Criação da Carteira de Criptoativos
        String walletAddress = "0xSAP" + Long.toHexString(user.getId() * 99991L).toUpperCase() + "Fe23Dd" + (1000 + secureRandom.nextInt(9000));
        CryptoWallet wallet = new CryptoWallet(user, walletAddress);
        wallet = cryptoWalletRepository.save(wallet);

        // Ativos cripto iniciais de boas-vindas
        cryptoAssetRepository.save(new CryptoAsset(wallet, "BTC", "Bitcoin", new BigDecimal("0.02500000")));
        cryptoAssetRepository.save(new CryptoAsset(wallet, "ETH", "Ethereum", new BigDecimal("0.75000000")));
        cryptoAssetRepository.save(new CryptoAsset(wallet, "SOL", "Solana", new BigDecimal("8.00000000")));
        cryptoAssetRepository.save(new CryptoAsset(wallet, "USDT", "Tether USD", new BigDecimal("500.00000000")));

        // Notificação de boas-vindas
        notificationService.createNotification(
                user.getId(),
                "Bem-vindo ao Banco SAP Cripto!",
                "Sua conta digital especializada em criptoativos foi criada com sucesso. Explore o mercado em tempo real!",
                NotificationType.SUCCESS
        );

        auditService.logAction(user.getId(), user.getEmail(), AuditAction.REGISTER, "AUTH", "Novo usuário cadastrado com sucesso", httpRequest);

        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = tokenProvider.generateRefreshToken(principal);

        return new AuthResponse(
                accessToken,
                refreshToken,
                tokenProvider.getAccessTokenExpirationMs(),
                buildUserSummary(user)
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = httpRequest != null ? httpRequest.getRemoteAddr() : "127.0.0.1";
        if (loginAttemptService.isIpBlocked(clientIp)) {
            throw new LockedException("Muitas tentativas incorretas a partir deste endereço de IP. Tente novamente em 15 minutos.");
        }

        String login = request.getLogin().trim();
        User user = userRepository.findByLoginIdentifier(login).orElse(null);

        if (user == null) {
            String cleanCpf = login.replaceAll("\\D", "");
            String formattedCpf = cleanCpf.length() == 11 ? formatCpf(cleanCpf) : cleanCpf;
            user = userRepository.findByCpf(formattedCpf)
                    .or(() -> userRepository.findByCpf(cleanCpf))
                    .orElse(null);
        }

        if (user == null) {
            loginAttemptService.loginFailed(login, clientIp);
            auditService.logAction(null, login, AuditAction.LOGIN_FAILED, "AUTH", "Falha de login: usuário inexistente", httpRequest);
            throw new BadCredentialsException("E-mail, usuário ou senha incorretos.");
        }

        if (user.getStatus() == UserStatus.BLOQUEADO) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new LockedException("Esta conta está temporariamente bloqueada por segurança. Tente novamente mais tarde.");
            } else if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(LocalDateTime.now())) {
                user.setStatus(UserStatus.ATIVO);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            } else {
                throw new LockedException("Esta conta foi bloqueada pela administração.");
            }
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptService.loginFailed(login, clientIp);
            auditService.logAction(user.getId(), user.getEmail(), AuditAction.LOGIN_FAILED, "AUTH", "Falha de login: senha incorreta", httpRequest);
            throw new BadCredentialsException("E-mail, usuário ou senha incorretos.");
        }

        loginAttemptService.loginSucceeded(login, clientIp);
        auditService.logAction(user.getId(), user.getEmail(), AuditAction.LOGIN_SUCCESS, "AUTH", "Login efetuado com sucesso", httpRequest);

        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = tokenProvider.generateRefreshToken(principal);

        return new AuthResponse(
                accessToken,
                refreshToken,
                tokenProvider.getAccessTokenExpirationMs(),
                buildUserSummary(user)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Refresh token inválido ou expirado. Faça login novamente.");
        }

        Long userId = tokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (user.getStatus() != UserStatus.ATIVO) {
            throw new AccountBlockedException("A conta do usuário está bloqueada.");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateAccessToken(principal);
        String newRefreshToken = tokenProvider.generateRefreshToken(principal);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                tokenProvider.getAccessTokenExpirationMs(),
                buildUserSummary(user)
        );
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase().trim();
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            String code = String.format("%06d", secureRandom.nextInt(900000) + 100000);
            user.setTwoFactorCode(code);
            user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);

            notificationService.createNotification(
                    user.getId(),
                    "Recuperação de Acesso",
                    "Seu código para redefinição de senha é: " + code + " (válido por 15 minutos).",
                    NotificationType.SECURITY
            );
        });

        auditService.logAction(null, email, AuditAction.PASSWORD_CHANGED, "AUTH", "Solicitação de recuperação de senha processada", httpRequest);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Código inválido ou expirado."));

        if (user.getTwoFactorCode() == null || !user.getTwoFactorCode().equals(request.getCode().trim())) {
            throw new BusinessException("Código de verificação incorreto.");
        }

        if (user.getTwoFactorExpiry() == null || user.getTwoFactorExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Código de verificação expirado. Solicite outro.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setTwoFactorCode(null);
        user.setTwoFactorExpiry(null);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        notificationService.createNotification(
                user.getId(),
                "Senha Alterada com Sucesso",
                "Sua senha foi redefinida com sucesso.",
                NotificationType.SECURITY
        );

        auditService.logAction(user.getId(), user.getEmail(), AuditAction.PASSWORD_CHANGED, "AUTH", "Senha redefinida com código de verificação", httpRequest);
    }

    public UserSummaryResponse buildUserSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                maskCpf(user.getCpf()),
                user.getBirthDate(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getProfilePhotoUrl(),
                user.getAddress(),
                user.getTransactionPinHash() != null,
                user.isTwoFactorEnabled(),
                user.getThemePreference()
        );
    }

    private String generateAccountNumber() {
        int number = 10000 + secureRandom.nextInt(90000);
        int digit = secureRandom.nextInt(10);
        String acc = number + "-" + digit;
        while (accountRepository.existsByAccountNumber(acc)) {
            number = 10000 + secureRandom.nextInt(90000);
            acc = number + "-" + digit;
        }
        return acc;
    }

    private String formatCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) return "***.***.***-**";
        String clean = cpf.replaceAll("\\D", "");
        if (clean.length() == 11) {
            return clean.substring(0, 3) + ".***.***-" + clean.substring(9);
        }
        return cpf;
    }
}
