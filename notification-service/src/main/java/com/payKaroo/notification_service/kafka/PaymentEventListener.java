package com.payKaroo.notification_service.kafka;

import com.payKaroo.notification_service.entity.Notification;
import com.payKaroo.notification_service.event.PaymentFailedEvent;
import com.payKaroo.notification_service.event.PaymentSuccessEvent;
import com.payKaroo.notification_service.event.RefundInitiatedEvent;
import com.payKaroo.notification_service.repository.NotificationRepository;
import com.payKaroo.notification_service.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    private static final String TEST_EMAIL = "test@example.com"; // temporary - real lookup coming Day 4

    public PaymentEventListener(EmailService emailService, NotificationRepository notificationRepository) {
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "payment.success", groupId = "notification-service-group")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        String message = "Your payment of " + event.getAmount() + " " + event.getCurrency()
                + " (Order: " + event.getOrderId() + ") was successful.";

        emailService.sendEmail(TEST_EMAIL, "Payment Successful", message);
        saveNotification(event.getUserId(), "PAYMENT_SUCCESS", message);
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        String message = "Your payment for Order " + event.getOrderId()
                + " failed. Reason: " + event.getReason();

        emailService.sendEmail(TEST_EMAIL, "Payment Failed", message);
        saveNotification(event.getUserId(), "PAYMENT_FAILED", message);
    }

    @KafkaListener(topics = "refund.initiated", groupId = "notification-service-group")
    public void handleRefundInitiated(RefundInitiatedEvent event) {
        String message = "A refund of " + event.getAmount()
                + " has been initiated for Payment ID: " + event.getPaymentId();

        emailService.sendEmail(TEST_EMAIL, "Refund Initiated", message);
        saveNotification(event.getUserId(), "REFUND_INITIATED", message);
    }

    private void saveNotification(Long userId, String type, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }
}
