package com.bancosap.dto.request;

import com.bancosap.enums.CardStatus;
import jakarta.validation.constraints.NotNull;

public class ToggleCardStatusRequest {

    @NotNull(message = "O ID do cartão é obrigatório.")
    private Long cardId;

    @NotNull(message = "O novo status é obrigatório.")
    private CardStatus status;

    public ToggleCardStatusRequest() {}

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public CardStatus getStatus() { return status; }
    public void setStatus(CardStatus status) { this.status = status; }
}
