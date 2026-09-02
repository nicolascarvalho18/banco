package com.bancosap.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class DepositRequest {

    @NotNull(message = "O valor do depósito é obrigatório.")
    @DecimalMin(value = "1.00", message = "O valor mínimo de depósito simulado é R$ 1,00.")
    private BigDecimal amount;

    private String method; // PIX_IMEDIATO, BOLETO_DEPOSITO, TED_RECEBIDA

    private String description;

    public DepositRequest() {}

    public DepositRequest(BigDecimal amount, String method, String description) {
        this.amount = amount;
        this.method = method;
        this.description = description;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
