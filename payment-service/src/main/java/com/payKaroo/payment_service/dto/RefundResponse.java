package com.payKaroo.payment_service.dto;

import java.math.BigDecimal;

public class RefundResponse {

    private Long refundId;
    private Long paymentId;
    private BigDecimal amount;
    private String status;
    private String razorpayRefundId;

    public RefundResponse(Long refundId, Long paymentId, BigDecimal amount, String status, String razorpayRefundId) {
        this.refundId = refundId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.status = status;
        this.razorpayRefundId = razorpayRefundId;
    }

    public Long getRefundId() { return refundId; }
    public Long getPaymentId() { return paymentId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getRazorpayRefundId() { return razorpayRefundId; }
}
