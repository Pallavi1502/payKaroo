package com.payKaroo.payment_service.kafka;

import com.payKaroo.payment_service.event.PaymentFailedEvent;
import com.payKaroo.payment_service.event.PaymentSuccessEvent;
import com.payKaroo.payment_service.event.RefundInitiatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String PAYMENT_SUCCESS_TOPIC = "payment.success";
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    public static final String REFUND_INITIATED_TOPIC = "refund.initiated";

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentSuccess(PaymentSuccessEvent event) {
        kafkaTemplate.send(PAYMENT_SUCCESS_TOPIC, event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        kafkaTemplate.send(PAYMENT_FAILED_TOPIC, event);
    }

    public void publishRefundInitiated(RefundInitiatedEvent event) {
        kafkaTemplate.send(REFUND_INITIATED_TOPIC, event);
    }
}
