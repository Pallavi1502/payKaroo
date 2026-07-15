package com.payKaroo.payment_service.dto;

import java.math.BigDecimal;

public class CreateOrderResponse {

    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String status;

    public CreateOrderResponse() {
    }

    public CreateOrderResponse(String orderId, BigDecimal amount, String currency, String status) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
