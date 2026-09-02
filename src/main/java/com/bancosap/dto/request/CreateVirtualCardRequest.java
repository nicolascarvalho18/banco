package com.bancosap.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateVirtualCardRequest {

    @NotBlank(message = "O nome gravado no cartão é obrigatório.")
    private String holderName;

    @NotNull(message = "O limite inicial é obrigatório.")
    @DecimalMin(value = "50.00", message = "O limite mínimo é R$ 50,00.")
    private BigDecimal spendingLimit;

    private boolean temporary = false;

    @NotBlank(message = "O PIN de segurança é obrigatório.")
    private String pin;

    public CreateVirtualCardRequest() {}

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public BigDecimal getSpendingLimit() { return spendingLimit; }
    public void setSpendingLimit(BigDecimal spendingLimit) { this.spendingLimit = spendingLimit; }

    public boolean isTemporary() { return temporary; }
    public void setTemporary(boolean temporary) { this.temporary = temporary; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
