package com.bancosap.entity;

import com.bancosap.enums.PixKeyType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pix_keys")
public class PixKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false, length = 20)
    private PixKeyType keyType;

    @Column(name = "key_value", nullable = false, unique = true, length = 100)
    private String keyValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PixKey() {}

    public PixKey(Account account, PixKeyType keyType, String keyValue) {
        this.account = account;
        this.keyType = keyType;
        this.keyValue = keyValue;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public PixKeyType getKeyType() { return keyType; }
    public void setKeyType(PixKeyType keyType) { this.keyType = keyType; }

    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
