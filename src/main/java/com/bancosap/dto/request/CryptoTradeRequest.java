package com.bancosap.dto.request;

import com.bancosap.enums.CryptoOperationType;
import com.bancosap.enums.CryptoSymbol;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CryptoTradeRequest {

    @NotNull(message = "O símbolo da criptomoeda é obrigatório.")
    private CryptoSymbol symbol;

    @NotNull(message = "A operação (COMPRA ou VENDA) é obrigatória.")
    private CryptoOperationType operationType;

    @NotNull(message = "O valor em BRL é obrigatório.")
    @DecimalMin(value = "10.00", message = "O valor mínimo de negociação é R$ 10,00.")
    private BigDecimal amountBrl;

    @NotBlank(message = "O PIN de segurança é obrigatório.")
    private String pin;

    public CryptoTradeRequest() {}

    public CryptoSymbol getSymbol() { return symbol; }
    public void setSymbol(CryptoSymbol symbol) { this.symbol = symbol; }

    public CryptoOperationType getOperationType() { return operationType; }
    public void setOperationType(CryptoOperationType operationType) { this.operationType = operationType; }

    public BigDecimal getAmountBrl() { return amountBrl; }
    public void setAmountBrl(BigDecimal amountBrl) { this.amountBrl = amountBrl; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
