package com.bancosap.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "simulated_orders")
public class SimulatedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "authentication_code", nullable = false, unique = true, length = 64)
    private String authenticationCode = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_type", nullable = false, length = 30)
    private String orderType; // COMPRA, VENDA, CONVERSAO

    @Column(name = "symbol_from", nullable = false, length = 20)
    private String symbolFrom;

    @Column(name = "symbol_to", nullable = false, length = 20)
    private String symbolTo;

    @Column(name = "amount_from", nullable = false, precision = 24, scale = 8)
    private BigDecimal amountFrom;

    @Column(name = "amount_to", nullable = false, precision = 24, scale = 8)
    private BigDecimal amountTo;

    @Column(name = "unit_price_brl", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPriceBrl;

    @Column(name = "fee_brl", nullable = false, precision = 19, scale = 4)
    private BigDecimal feeBrl = BigDecimal.ZERO;

    @Column(name = "slippage_tolerance", precision = 5, scale = 2)
    private BigDecimal slippageTolerance = new BigDecimal("0.50");

    @Column(nullable = false, length = 20)
    private String status = "EXECUTADA";

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SimulatedOrder() {}

    public SimulatedOrder(User user, String orderType, String symbolFrom, String symbolTo, BigDecimal amountFrom, BigDecimal amountTo, BigDecimal unitPriceBrl, BigDecimal feeBrl, String idempotencyKey) {
        this.user = user;
        this.orderType = orderType;
        this.symbolFrom = symbolFrom;
        this.symbolTo = symbolTo;
        this.amountFrom = amountFrom;
        this.amountTo = amountTo;
        this.unitPriceBrl = unitPriceBrl;
        this.feeBrl = feeBrl;
        this.idempotencyKey = idempotencyKey;
        this.authenticationCode = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAuthenticationCode() { return authenticationCode; }
    public void setAuthenticationCode(String authenticationCode) { this.authenticationCode = authenticationCode; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getSymbolFrom() { return symbolFrom; }
    public void setSymbolFrom(String symbolFrom) { this.symbolFrom = symbolFrom; }

    public String getSymbolTo() { return symbolTo; }
    public void setSymbolTo(String symbolTo) { this.symbolTo = symbolTo; }

    public BigDecimal getAmountFrom() { return amountFrom; }
    public void setAmountFrom(BigDecimal amountFrom) { this.amountFrom = amountFrom; }

    public BigDecimal getAmountTo() { return amountTo; }
    public void setAmountTo(BigDecimal amountTo) { this.amountTo = amountTo; }

    public BigDecimal getUnitPriceBrl() { return unitPriceBrl; }
    public void setUnitPriceBrl(BigDecimal unitPriceBrl) { this.unitPriceBrl = unitPriceBrl; }

    public BigDecimal getFeeBrl() { return feeBrl; }
    public void setFeeBrl(BigDecimal feeBrl) { this.feeBrl = feeBrl; }

    public BigDecimal getSlippageTolerance() { return slippageTolerance; }
    public void setSlippageTolerance(BigDecimal slippageTolerance) { this.slippageTolerance = slippageTolerance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
