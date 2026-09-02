package com.bancosap.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SimulatedSellRequest {

    @NotBlank(message = "O símbolo do criptoativo é obrigatório.")
    private String symbol;

    @NotNull(message = "A quantidade de criptoativo a vender é obrigatória.")
    @DecimalMin(value = "0.00000001", message = "A quantidade mínima de venda é 0.00000001.")
    private BigDecimal cryptoAmount;

    @NotBlank(message = "O PIN de segurança é obrigatório.")
    private String pin;

    private String idempotencyKey;

    public SimulatedSellRequest() {}

    public SimulatedSellRequest(String symbol, BigDecimal cryptoAmount, String pin) {
        this.symbol = symbol;
        this.cryptoAmount = cryptoAmount;
        this.pin = pin;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getCryptoAmount() { return cryptoAmount; }
    public void setCryptoAmount(BigDecimal cryptoAmount) { this.cryptoAmount = cryptoAmount; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
