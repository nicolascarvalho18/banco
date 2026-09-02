package com.bancosap.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AdminUpdateLimitsRequest {

    @NotNull(message = "O ID do usuário é obrigatório.")
    private Long userId;

    @NotNull(message = "O limite de crédito é obrigatório.")
    @DecimalMin(value = "0.00", message = "O limite não pode ser negativo.")
    private BigDecimal creditLimit;

    @NotNull(message = "O limite diário de PIX é obrigatório.")
    @DecimalMin(value = "0.00", message = "O limite não pode ser negativo.")
    private BigDecimal dailyPixLimit;

    @NotNull(message = "O limite noturno de PIX é obrigatório.")
    @DecimalMin(value = "0.00", message = "O limite não pode ser negativo.")
    private BigDecimal nightlyPixLimit;

    public AdminUpdateLimitsRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getDailyPixLimit() { return dailyPixLimit; }
    public void setDailyPixLimit(BigDecimal dailyPixLimit) { this.dailyPixLimit = dailyPixLimit; }

    public BigDecimal getNightlyPixLimit() { return nightlyPixLimit; }
    public void setNightlyPixLimit(BigDecimal nightlyPixLimit) { this.nightlyPixLimit = nightlyPixLimit; }
}
