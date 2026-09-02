package com.bancosap.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BillValidationResponse {
    private String barcode;
    private String formattedBarcode;
    private String recipientName;
    private LocalDate dueDate;
    private BigDecimal amount;
    private boolean valid;
    private String bankName;
    private String message;

    public BillValidationResponse() {}

    public BillValidationResponse(String barcode, String formattedBarcode, String recipientName,
                                  LocalDate dueDate, BigDecimal amount, boolean valid, String bankName, String message) {
        this.barcode = barcode;
        this.formattedBarcode = formattedBarcode;
        this.recipientName = recipientName;
        this.dueDate = dueDate;
        this.amount = amount;
        this.valid = valid;
        this.bankName = bankName;
        this.message = message;
    }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getFormattedBarcode() { return formattedBarcode; }
    public void setFormattedBarcode(String formattedBarcode) { this.formattedBarcode = formattedBarcode; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
