package com.bancosap.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SimulatedBuyRequest {

    @NotBlank(message = "O símbolo do criptoativo é obrigatório.")
    private String symbol;

    @NotNull(message = "O valor da compra em BRL é obrigatório.")
    @DecimalMin(value = "1.00", message = "O valor mínimo de compra é de R$ 1,00.")
    private BigDecimal amountBrl;

    @NotBlank(message = "O PIN de segurança é obrigatório.")
    private String pin;

    private String idempotencyKey;

    public SimulatedBuyRequest() {}

    public SimulatedBuyRequest(String symbol, BigDecimal amountBrl, String pin) {
        this.symbol = symbol;
        this.amountBrl = amountBrl;
        this.pin = pin;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getAmountBrl() { return amountBrl; }
    public void setAmountBrl(BigDecimal amountBrl) { this.amountBrl = amountBrl; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
