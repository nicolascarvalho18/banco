package com.bancosap.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SimulatedOrderResponse {
    private String authenticationCode;
    private String orderType;
    private String symbolFrom;
    private String symbolTo;
    private BigDecimal amountFrom;
    private BigDecimal amountTo;
    private BigDecimal unitPriceBrl;
    private BigDecimal feeBrl;
    private String status;
    private LocalDateTime createdAt;

    public SimulatedOrderResponse() {}

    public SimulatedOrderResponse(String authenticationCode, String orderType, String symbolFrom, String symbolTo, BigDecimal amountFrom, BigDecimal amountTo, BigDecimal unitPriceBrl, BigDecimal feeBrl, String status, LocalDateTime createdAt) {
        this.authenticationCode = authenticationCode;
        this.orderType = orderType;
        this.symbolFrom = symbolFrom;
        this.symbolTo = symbolTo;
        this.amountFrom = amountFrom;
        this.amountTo = amountTo;
        this.unitPriceBrl = unitPriceBrl;
        this.feeBrl = feeBrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getAuthenticationCode() { return authenticationCode; }
    public String getOrderType() { return orderType; }
    public String getSymbolFrom() { return symbolFrom; }
    public String getSymbolTo() { return symbolTo; }
    public BigDecimal getAmountFrom() { return amountFrom; }
    public BigDecimal getAmountTo() { return amountTo; }
    public BigDecimal getUnitPriceBrl() { return unitPriceBrl; }
    public BigDecimal getFeeBrl() { return feeBrl; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
