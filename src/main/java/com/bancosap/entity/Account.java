package com.bancosap.entity;

import com.bancosap.enums.AccountStatus;
import com.bancosap.enums.AccountType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "agency_number", nullable = false, length = 10)
    private String agencyNumber = "0001";

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private AccountType accountType = AccountType.CORRENTE;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "savings_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal savingsBalance = BigDecimal.ZERO;

    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit = new BigDecimal("5000.00");

    @Column(name = "daily_pix_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyPixLimit = new BigDecimal("10000.00");

    @Column(name = "nightly_pix_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal nightlyPixLimit = new BigDecimal("1000.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ATIVO;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PixKey> pixKeys = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VirtualCard> virtualCards = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillPayment> billPayments = new ArrayList<>();

    public Account() {}

    public Account(User user, String accountNumber, BigDecimal initialBalance) {
        this.user = user;
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getDailyPixLimit() { return dailyPixLimit; }
    public void setDailyPixLimit(BigDecimal dailyPixLimit) { this.dailyPixLimit = dailyPixLimit; }

    public BigDecimal getNightlyPixLimit() { return nightlyPixLimit; }
    public void setNightlyPixLimit(BigDecimal nightlyPixLimit) { this.nightlyPixLimit = nightlyPixLimit; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PixKey> getPixKeys() { return pixKeys; }
    public void setPixKeys(List<PixKey> pixKeys) { this.pixKeys = pixKeys; }

    public List<VirtualCard> getVirtualCards() { return virtualCards; }
    public void setVirtualCards(List<VirtualCard> virtualCards) { this.virtualCards = virtualCards; }

    public List<BillPayment> getBillPayments() { return billPayments; }
    public void setBillPayments(List<BillPayment> billPayments) { this.billPayments = billPayments; }
}
