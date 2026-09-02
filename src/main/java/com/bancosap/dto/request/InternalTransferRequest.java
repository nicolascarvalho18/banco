package com.bancosap.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class InternalTransferRequest {

    @NotBlank(message = "O destinatário (username ou e-mail) é obrigatório.")
    private String recipientIdentifier;

    @NotBlank(message = "O símbolo do ativo a transferir é obrigatório.")
    private String symbol;

    @NotNull(message = "A quantidade é obrigatória.")
    @DecimalMin(value = "0.00000001", message = "A quantidade mínima de transferência é 0.00000001.")
    private BigDecimal amount;

    private String description;

    @NotBlank(message = "O PIN de segurança é obrigatório.")
    private String pin;

    public InternalTransferRequest() {}

    public InternalTransferRequest(String recipientIdentifier, String symbol, BigDecimal amount, String description, String pin) {
        this.recipientIdentifier = recipientIdentifier;
        this.symbol = symbol;
        this.amount = amount;
        this.description = description;
        this.pin = pin;
    }

    public String getRecipientIdentifier() { return recipientIdentifier; }
    public void setRecipientIdentifier(String recipientIdentifier) { this.recipientIdentifier = recipientIdentifier; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
