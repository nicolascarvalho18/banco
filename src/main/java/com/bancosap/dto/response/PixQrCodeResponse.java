package com.bancosap.dto.response;

import java.math.BigDecimal;

public class PixQrCodeResponse {
    private String payload;
    private String qrCodeBase64;
    private BigDecimal amount;
    private String receiverName;
    private String key;
    private String city;

    public PixQrCodeResponse() {}

    public PixQrCodeResponse(String payload, String qrCodeBase64, BigDecimal amount, String receiverName, String key, String city) {
        this.payload = payload;
        this.qrCodeBase64 = qrCodeBase64;
        this.amount = amount;
        this.receiverName = receiverName;
        this.key = key;
        this.city = city;
    }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
