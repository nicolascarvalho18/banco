package com.bancosap.service;

import com.bancosap.entity.AuditLog;
import com.bancosap.enums.AuditAction;
import com.bancosap.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(Long userId, String userEmail, AuditAction action, String resource,
                          String details, HttpServletRequest request) {
        String ipAddress = "127.0.0.1";
        String userAgent = "Unknown";

        if (request != null) {
            String clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = request.getRemoteAddr();
            }
            ipAddress = clientIp;
            userAgent = request.getHeader("User-Agent");
        }

        AuditLog log = new AuditLog(userId, userEmail, action, resource, ipAddress, userAgent, details);
        auditLogRepository.save(log);
    }
}
