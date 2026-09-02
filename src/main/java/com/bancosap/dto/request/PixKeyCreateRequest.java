package com.bancosap.dto.request;

import com.bancosap.enums.PixKeyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PixKeyCreateRequest {

    @NotNull(message = "O tipo de chave PIX é obrigatório.")
    private PixKeyType keyType;

    @NotBlank(message = "O valor da chave PIX é obrigatório.")
    private String keyValue;

    public PixKeyCreateRequest() {}

    public PixKeyCreateRequest(PixKeyType keyType, String keyValue) {
        this.keyType = keyType;
        this.keyValue = keyValue;
    }

    public PixKeyType getKeyType() { return keyType; }
    public void setKeyType(PixKeyType keyType) { this.keyType = keyType; }

    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }
}
