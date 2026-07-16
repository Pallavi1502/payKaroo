package com.payKaroo.payment_service.event;

import java.math.BigDecimal;

public class RefundInitiatedEvent {
    private Long userId;
    private String email;
    private Long refundId;
    private Long paymentId;
    private BigDecimal amount;

    public RefundInitiatedEvent() {
    }

    public RefundInitiatedEvent(Long userId, String email, Long refundId, Long paymentId, BigDecimal amount) {
        this.userId = userId;
        this.email = email;
        this.refundId = refundId;
        this.paymentId = paymentId;
        this.amount = amount;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(Long userId) { this.email = email; }

    public Long getRefundId() { return refundId; }
    public void setRefundId(Long refundId) { this.refundId = refundId; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
