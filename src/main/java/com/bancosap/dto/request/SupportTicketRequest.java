package com.bancosap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupportTicketRequest {

    @NotBlank(message = "O assunto do chamado é obrigatório.")
    @Size(min = 5, max = 150, message = "O assunto deve ter entre 5 e 150 caracteres.")
    private String subject;

    @NotBlank(message = "A categoria é obrigatória.")
    private String category;

    @NotBlank(message = "A mensagem inicial é obrigatória.")
    @Size(min = 10, message = "A mensagem deve conter pelo menos 10 caracteres.")
    private String message;

    public SupportTicketRequest() {}

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
