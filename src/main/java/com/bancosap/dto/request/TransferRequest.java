package com.bancosap.dto.request;

import com.bancosap.enums.TransactionCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class TransferRequest {

    @NotBlank(message = "Informe a conta, CPF ou e-mail do destinatário.")
    private String destinationIdentifier;

    @NotNull(message = "O valor da transferência é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor mínimo para transferência é R$ 0,01.")
    private BigDecimal amount;

    private String description;

    private TransactionCategory category = TransactionCategory.TRANSFERENCIA;

    @NotBlank(message = "O PIN de segurança ou confirmação é obrigatório.")
    @Size(min = 4, max = 6, message = "O PIN deve conter de 4 a 6 dígitos.")
    private String pin;

    public TransferRequest() {}

    public String getDestinationIdentifier() { return destinationIdentifier; }
    public void setDestinationIdentifier(String destinationIdentifier) { this.destinationIdentifier = destinationIdentifier; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionCategory getCategory() { return category; }
    public void setCategory(TransactionCategory category) { this.category = category; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
