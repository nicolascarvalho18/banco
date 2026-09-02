package com.bancosap.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SimulatePurchaseRequest {

    @NotBlank(message = "O token ou número do cartão é obrigatório.")
    private String cardNumberToken;

    @NotBlank(message = "O código CVV é obrigatório.")
    private String cvv;

    @NotNull(message = "O valor da compra é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
    private BigDecimal amount;

    @NotBlank(message = "O nome do estabelecimento é obrigatório.")
    private String merchantName;

    public SimulatePurchaseRequest() {}

    public String getCardNumberToken() { return cardNumberToken; }
    public void setCardNumberToken(String cardNumberToken) { this.cardNumberToken = cardNumberToken; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
}
