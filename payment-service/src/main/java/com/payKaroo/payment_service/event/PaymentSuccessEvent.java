package com.payKaroo.payment_service.event;

import java.math.BigDecimal;

public class PaymentSuccessEvent {
    private Long userId;
    private Long paymentId;
    private String orderId;
    private BigDecimal amount;
    private String currency;

    public PaymentSuccessEvent() {
    }

    public PaymentSuccessEvent(Long userId, Long paymentId, String orderId, BigDecimal amount, String currency) {
        this.userId = userId;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

}
