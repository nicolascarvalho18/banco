package com.bancosap.service;

import com.bancosap.dto.request.AdminToggleUserStatusRequest;
import com.bancosap.dto.response.AdminDashboardResponse;
import com.bancosap.dto.response.UserSummaryResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.User;
import com.bancosap.enums.RoleName;
import com.bancosap.enums.UserStatus;
import com.bancosap.exception.BusinessException;
import com.bancosap.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private SupportTicketRepository ticketRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminService adminService;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        adminUser = new User("Admin", "000.000.000-00", LocalDate.of(1990, 1, 1), "(11) 99999-0000", "admin@bancosap.com.br", "pwd", RoleName.ROLE_ADMIN);
        adminUser.setId(1L);

        normalUser = new User("Cliente", "123.456.789-00", LocalDate.of(1995, 5, 5), "(11) 98888-1111", "cliente@bancosap.com.br", "pwd", RoleName.ROLE_CLIENTE);
        normalUser.setId(2L);
    }

    @Test
    @DisplayName("Deve calcular métricas do dashboard administrativo corretamente")
    void shouldCalculateDashboardMetrics() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.findAll()).thenReturn(List.of(adminUser, normalUser));

        Account acc = new Account(normalUser, "12345-6", new BigDecimal("15000.00"));
        when(accountRepository.findAll()).thenReturn(List.of(acc));
        when(transactionRepository.count()).thenReturn(50L);
        when(ticketRepository.count()).thenReturn(3L);

        AdminDashboardResponse metrics = adminService.getDashboardMetrics();

        assertNotNull(metrics);
        assertEquals(10L, metrics.getTotalUsers());
        assertEquals(2L, metrics.getActiveUsers());
        assertEquals(new BigDecimal("15000.00"), metrics.getTotalDepositBalance());
        assertEquals(50L, metrics.getTotalTransactionsCount());
    }

    @Test
    @DisplayName("Deve bloquear conta de cliente comum e registrar auditoria")
    void shouldBlockNormalUser() {
        AdminToggleUserStatusRequest request = new AdminToggleUserStatusRequest();
        request.setUserId(2L);
        request.setStatus(UserStatus.BLOQUEADO);
        request.setReason("Suspeita de fraude");

        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        when(userRepository.save(any())).thenReturn(normalUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        UserSummaryResponse response = adminService.toggleUserStatus(1L, request, null);

        assertNotNull(response);
        assertEquals(UserStatus.BLOQUEADO, normalUser.getStatus());
        verify(auditService, times(1)).logAction(eq(1L), any(), any(), eq("ADMIN_USERS"), any(), any());
    }

    @Test
    @DisplayName("Deve impedir bloqueio de outro Administrador")
    void shouldPreventBlockingAnotherAdmin() {
        AdminToggleUserStatusRequest request = new AdminToggleUserStatusRequest();
        request.setUserId(1L);
        request.setStatus(UserStatus.BLOQUEADO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(BusinessException.class, () -> adminService.toggleUserStatus(1L, request, null));
    }
}
