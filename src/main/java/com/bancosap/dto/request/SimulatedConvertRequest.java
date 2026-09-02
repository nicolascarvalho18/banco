package com.bancosap.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SimulatedConvertRequest {

    @NotBlank(message = "A moeda de origem é obrigatória.")
    private String fromSymbol;

    @NotBlank(message = "A moeda de destino é obrigatória.")
    private String toSymbol;

    @NotNull(message = "A quantidade de origem é obrigatória.")
    @DecimalMin(value = "0.00000001", message = "A quantidade mínima para conversão é 0.00000001.")
    private BigDecimal fromAmount;

    @NotBlank(message = "O PIN de segurança é obrigatório.")
    private String pin;

    private String idempotencyKey;

    public SimulatedConvertRequest() {}

    public SimulatedConvertRequest(String fromSymbol, String toSymbol, BigDecimal fromAmount, String pin) {
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
        this.fromAmount = fromAmount;
        this.pin = pin;
    }

    public String getFromSymbol() { return fromSymbol; }
    public void setFromSymbol(String fromSymbol) { this.fromSymbol = fromSymbol; }

    public String getToSymbol() { return toSymbol; }
    public void setToSymbol(String toSymbol) { this.toSymbol = toSymbol; }

    public BigDecimal getFromAmount() { return fromAmount; }
    public void setFromAmount(BigDecimal fromAmount) { this.fromAmount = fromAmount; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
