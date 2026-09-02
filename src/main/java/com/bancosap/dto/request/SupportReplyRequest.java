package com.bancosap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SupportReplyRequest {

    @NotNull(message = "O ID do chamado é obrigatório.")
    private Long ticketId;

    @NotBlank(message = "A mensagem de resposta não pode estar vazia.")
    private String message;

    public SupportReplyRequest() {}

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
