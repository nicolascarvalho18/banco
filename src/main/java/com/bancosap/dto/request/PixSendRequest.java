package com.bancosap.dto.request;

import com.bancosap.enums.PixKeyType;
import com.bancosap.enums.TransactionCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PixSendRequest {

    private PixKeyType keyType;

    @NotBlank(message = "Informe a chave PIX de destino ou código copia e cola.")
    private String keyValue;

    @NotNull(message = "O valor do PIX é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor mínimo para PIX é R$ 0,01.")
    private BigDecimal amount;

    private String description;

    private TransactionCategory category = TransactionCategory.PIX;

    @NotBlank(message = "O PIN de segurança ou confirmação é obrigatório.")
    private String pin;

    public PixSendRequest() {}

    public PixKeyType getKeyType() { return keyType; }
    public void setKeyType(PixKeyType keyType) { this.keyType = keyType; }

    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionCategory getCategory() { return category; }
    public void setCategory(TransactionCategory category) { this.category = category; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
