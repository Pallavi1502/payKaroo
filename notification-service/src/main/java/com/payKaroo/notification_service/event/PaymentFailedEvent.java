package com.payKaroo.notification_service.event;

public class PaymentFailedEvent {

    private Long userId;
    private String email;
    private String orderId;
    private String reason;

    public PaymentFailedEvent() {
    }

    public PaymentFailedEvent(Long userId, String email, String orderId, String reason) {
        this.userId = userId;
        this.email = email;
        this.orderId = orderId;
        this.reason = reason;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(Long userId) { this.email = email; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
