package com.bancosap.dto.response;

import com.bancosap.enums.PixKeyType;
import java.time.LocalDateTime;

public class PixKeyResponse {
    private Long id;
    private PixKeyType keyType;
    private String keyValue;
    private LocalDateTime createdAt;

    public PixKeyResponse() {}

    public PixKeyResponse(Long id, PixKeyType keyType, String keyValue, LocalDateTime createdAt) {
        this.id = id;
        this.keyType = keyType;
        this.keyValue = keyValue;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PixKeyType getKeyType() { return keyType; }
    public void setKeyType(PixKeyType keyType) { this.keyType = keyType; }

    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
