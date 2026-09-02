package com.bancosap.security;

import com.bancosap.entity.User;
import com.bancosap.enums.UserStatus;
import com.bancosap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final int maxAttempts;
    private final int lockoutDurationMinutes;
    private final ConcurrentHashMap<String, Integer> ipAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> ipLockout = new ConcurrentHashMap<>();

    public LoginAttemptService(
            UserRepository userRepository,
            @Value("${bancosap.security.rate-limit.max-failed-login-attempts:5}") int maxAttempts,
            @Value("${bancosap.security.rate-limit.lockout-duration-minutes:15}") int lockoutDurationMinutes) {
        this.userRepository = userRepository;
        this.maxAttempts = maxAttempts;
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    @Transactional
    public void loginFailed(String login, String ipAddress) {
        // Incrementa tentativas por IP
        if (ipAddress != null) {
            int attempts = ipAttempts.getOrDefault(ipAddress, 0) + 1;
            ipAttempts.put(ipAddress, attempts);
            if (attempts >= maxAttempts * 2) {
                ipLockout.put(ipAddress, LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            }
        }

        // Incrementa no usuário
        User user = null;
        if (login.contains("@")) {
            user = userRepository.findByEmailIgnoreCase(login).orElse(null);
        } else {
            user = userRepository.findByCpf(login).orElse(null);
        }

        if (user != null) {
            int failed = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failed);
            if (failed >= maxAttempts) {
                user.setStatus(UserStatus.BLOQUEADO);
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            }
            userRepository.save(user);
        }
    }

    @Transactional
    public void loginSucceeded(String login, String ipAddress) {
        if (ipAddress != null) {
            ipAttempts.remove(ipAddress);
            ipLockout.remove(ipAddress);
        }

        User user = null;
        if (login.contains("@")) {
            user = userRepository.findByEmailIgnoreCase(login).orElse(null);
        } else {
            user = userRepository.findByCpf(login).orElse(null);
        }

        if (user != null && user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

    public boolean isIpBlocked(String ipAddress) {
        if (ipAddress == null) return false;
        LocalDateTime lockout = ipLockout.get(ipAddress);
        if (lockout != null) {
            if (lockout.isAfter(LocalDateTime.now())) {
                return true;
            } else {
                ipLockout.remove(ipAddress);
                ipAttempts.remove(ipAddress);
            }
        }
        return false;
    }
}
