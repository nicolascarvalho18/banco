package com.bancosap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SetPinRequest {

    @NotBlank(message = "O novo PIN é obrigatório.")
    @Pattern(regexp = "^\\d{4,6}$", message = "O PIN deve conter exatamente entre 4 e 6 números.")
    private String pin;

    @NotBlank(message = "A senha da conta é obrigatória para confirmar.")
    private String password;

    public SetPinRequest() {}

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
