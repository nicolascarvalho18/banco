package com.bancosap.dto.response;

import java.math.BigDecimal;

public class AdminDashboardResponse {
    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;
    private BigDecimal totalDepositBalance;
    private BigDecimal totalVolume24h;
    private long totalTransactionsCount;
    private long pendingTicketsCount;
    private long securityAlertsCount;

    public AdminDashboardResponse() {}

    public AdminDashboardResponse(long totalUsers, long activeUsers, long blockedUsers,
                                  BigDecimal totalDepositBalance, BigDecimal totalVolume24h,
                                  long totalTransactionsCount, long pendingTicketsCount, long securityAlertsCount) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.blockedUsers = blockedUsers;
        this.totalDepositBalance = totalDepositBalance;
        this.totalVolume24h = totalVolume24h;
        this.totalTransactionsCount = totalTransactionsCount;
        this.pendingTicketsCount = pendingTicketsCount;
        this.securityAlertsCount = securityAlertsCount;
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getBlockedUsers() { return blockedUsers; }
    public void setBlockedUsers(long blockedUsers) { this.blockedUsers = blockedUsers; }

    public BigDecimal getTotalDepositBalance() { return totalDepositBalance; }
    public void setTotalDepositBalance(BigDecimal totalDepositBalance) { this.totalDepositBalance = totalDepositBalance; }

    public BigDecimal getTotalVolume24h() { return totalVolume24h; }
    public void setTotalVolume24h(BigDecimal totalVolume24h) { this.totalVolume24h = totalVolume24h; }

    public long getTotalTransactionsCount() { return totalTransactionsCount; }
    public void setTotalTransactionsCount(long totalTransactionsCount) { this.totalTransactionsCount = totalTransactionsCount; }

    public long getPendingTicketsCount() { return pendingTicketsCount; }
    public void setPendingTicketsCount(long pendingTicketsCount) { this.pendingTicketsCount = pendingTicketsCount; }

    public long getSecurityAlertsCount() { return securityAlertsCount; }
    public void setSecurityAlertsCount(long securityAlertsCount) { this.securityAlertsCount = securityAlertsCount; }
}
