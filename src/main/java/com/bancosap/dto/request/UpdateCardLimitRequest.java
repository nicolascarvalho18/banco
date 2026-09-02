package com.bancosap.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class UpdateCardLimitRequest {

    @NotNull(message = "O ID do cartão é obrigatório.")
    private Long cardId;

    @NotNull(message = "O novo limite é obrigatório.")
    @DecimalMin(value = "0.00", message = "O limite não pode ser negativo.")
    private BigDecimal newLimit;

    @NotBlank(message = "O PIN de segurança é obrigatório.")
    private String pin;

    public UpdateCardLimitRequest() {}

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public BigDecimal getNewLimit() { return newLimit; }
    public void setNewLimit(BigDecimal newLimit) { this.newLimit = newLimit; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
