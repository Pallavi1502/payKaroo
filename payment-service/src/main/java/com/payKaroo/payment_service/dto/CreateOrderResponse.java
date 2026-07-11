package com.payKaroo.payment_service.dto;

import java.math.BigDecimal;

public class CreateOrderResponse {

    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String status;

    public CreateOrderResponse(String orderId, BigDecimal amount, String currency, String status) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
}
