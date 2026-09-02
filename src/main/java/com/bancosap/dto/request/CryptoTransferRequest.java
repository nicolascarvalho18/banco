package com.bancosap.dto.request;

import com.bancosap.enums.CryptoSymbol;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CryptoTransferRequest {

    @NotBlank(message = "O endereço da carteira de destino é obrigatório.")
    private String destinationWalletAddress;

    @NotNull(message = "O símbolo da moeda é obrigatório.")
    private CryptoSymbol symbol;

    @NotNull(message = "A quantidade de criptoativo é obrigatória.")
    @DecimalMin(value = "0.00000001", message = "A quantidade mínima é 0.00000001.")
    private BigDecimal quantity;

    @NotBlank(message = "O PIN de segurança é obrigatório.")
    private String pin;

    public CryptoTransferRequest() {}

    public String getDestinationWalletAddress() { return destinationWalletAddress; }
    public void setDestinationWalletAddress(String destinationWalletAddress) { this.destinationWalletAddress = destinationWalletAddress; }

    public CryptoSymbol getSymbol() { return symbol; }
    public void setSymbol(CryptoSymbol symbol) { this.symbol = symbol; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
