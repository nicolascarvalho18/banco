package com.bancosap.dto.response;

import com.bancosap.enums.AccountStatus;
import com.bancosap.enums.AccountType;
import java.math.BigDecimal;

public class AccountSummaryResponse {
    private Long id;
    private String agencyNumber;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private BigDecimal savingsBalance;
    private BigDecimal totalBalance;
    private BigDecimal creditLimit;
    private BigDecimal dailyPixLimit;
    private BigDecimal nightlyPixLimit;
    private AccountStatus status;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;

    public AccountSummaryResponse() {}

    public AccountSummaryResponse(Long id, String agencyNumber, String accountNumber, AccountType accountType,
                                  BigDecimal balance, BigDecimal savingsBalance, BigDecimal creditLimit,
                                  BigDecimal dailyPixLimit, BigDecimal nightlyPixLimit, AccountStatus status,
                                  BigDecimal monthlyIncome, BigDecimal monthlyExpenses) {
        this.id = id;
        this.agencyNumber = agencyNumber;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.savingsBalance = savingsBalance;
        this.totalBalance = balance.add(savingsBalance);
        this.creditLimit = creditLimit;
        this.dailyPixLimit = dailyPixLimit;
        this.nightlyPixLimit = nightlyPixLimit;
        this.status = status;
        this.monthlyIncome = monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO;
        this.monthlyExpenses = monthlyExpenses != null ? monthlyExpenses : BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAgencyNumber() { return agencyNumber; }
    public void setAgencyNumber(String agencyNumber) { this.agencyNumber = agencyNumber; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getSavingsBalance() { return savingsBalance; }
    public void setSavingsBalance(BigDecimal savingsBalance) { this.savingsBalance = savingsBalance; }

    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getDailyPixLimit() { return dailyPixLimit; }
    public void setDailyPixLimit(BigDecimal dailyPixLimit) { this.dailyPixLimit = dailyPixLimit; }

    public BigDecimal getNightlyPixLimit() { return nightlyPixLimit; }
    public void setNightlyPixLimit(BigDecimal nightlyPixLimit) { this.nightlyPixLimit = nightlyPixLimit; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public BigDecimal getMonthlyExpenses() { return monthlyExpenses; }
    public void setMonthlyExpenses(BigDecimal monthlyExpenses) { this.monthlyExpenses = monthlyExpenses; }
}
