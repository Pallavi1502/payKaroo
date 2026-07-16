package com.payKaroo.payment_service.service;


import com.payKaroo.payment_service.dto.CreateOrderRequest;
import com.payKaroo.payment_service.dto.CreateOrderResponse;
import com.payKaroo.payment_service.dto.VerifyPaymentRequest;
import com.payKaroo.payment_service.entity.Payment;
import com.payKaroo.payment_service.entity.PaymentStatus;
import com.payKaroo.payment_service.entity.RefundStatus;
import com.payKaroo.payment_service.event.PaymentFailedEvent;
import com.payKaroo.payment_service.event.PaymentSuccessEvent;
import com.payKaroo.payment_service.exception.PaymentNotFoundException;
import com.payKaroo.payment_service.exception.PaymentVerificationException;
import com.payKaroo.payment_service.kafka.PaymentEventProducer;
import com.payKaroo.payment_service.repository.PaymentRepository;
import com.payKaroo.payment_service.repository.RefundRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentEventProducer eventProducer;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    public PaymentService(RazorpayClient razorpayClient, PaymentRepository paymentRepository,
                          RefundRepository refundRepository, PaymentEventProducer eventProducer) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.eventProducer = eventProducer;
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request) throws RazorpayException {

        int amountInSmallestUnit = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInSmallestUnit);
        orderRequest.put("currency", request.getCurrency());
        orderRequest.put("payment_capture", 1);

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        Payment payment = new Payment();
        payment.setUserId(request.getUserId());
        payment.setOrderId(razorpayOrderId);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);

        return new CreateOrderResponse(
                razorpayOrderId,
                request.getAmount(),
                request.getCurrency(),
                PaymentStatus.PENDING.name()
        );

    }

    public String verifyPayment(VerifyPaymentRequest request) {

        Payment payment = paymentRepository.findByOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment found for order ID: " + request.getRazorpayOrderId()));

        boolean isValidSignature = verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValidSignature) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            eventProducer.publishPaymentFailed(new PaymentFailedEvent(
                    payment.getUserId(), "test@example.com",payment.getOrderId(), "Signature verification failed"));

            throw new PaymentVerificationException("Payment signature verification failed");
        }

        payment.setPaymentId(request.getRazorpayPaymentId());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        eventProducer.publishPaymentSuccess(new PaymentSuccessEvent(
                payment.getUserId(), "test@example.com", payment.getId(), payment.getOrderId(),
                payment.getAmount(), payment.getCurrency()));

        return "Payment verified successfully";
    }


    private boolean verifySignature(String orderId, String paymentId, String signature){
        try{
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String generatedSignature = hexString.toString();
            return generatedSignature.equals(signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new PaymentVerificationException("Error verifying payment signature");
        }
    }

    public Page<Payment> getPaymentHistory(Long userId, PaymentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (status != null) {
            return paymentRepository.findByUserIdAndStatus(userId, status, pageable);
        }
        return paymentRepository.findByUserId(userId, pageable);
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("No payment found with ID: " + paymentId));
    }

    public void processWebhook(String payload, String razorpaySignature){
        boolean isValid = verifyWebhookSignature(payload, razorpaySignature);

        if (!isValid) {
            throw new PaymentVerificationException("Invalid webhook signature");
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");

        switch (eventType) {
            case "payment.captured" -> handlePaymentCaptured(event);
            case "payment.failed" -> handlePaymentFailed(event);
            case "refund.processed" -> handleRefundProcessed(event);
            default -> {
                // Unhandled event type - log and ignore
            }
        }
    }

    private boolean verifyWebhookSignature(String payload, String signature){
        try{
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(signature);
        }catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new PaymentVerificationException("Error verifying webhook signature");
        }
    }

    private void handlePaymentCaptured(JSONObject event) {
        JSONObject paymentEntity = event.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayOrderId = paymentEntity.getString("order_id");
        String razorpayPaymentId = paymentEntity.getString("id");

        paymentRepository.findByOrderId(razorpayOrderId).ifPresent(payment -> {
            payment.setPaymentId(razorpayPaymentId);
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
        });
    }

    private void handlePaymentFailed(JSONObject event) {
        JSONObject paymentEntity = event.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayOrderId = paymentEntity.getString("order_id");

        paymentRepository.findByOrderId(razorpayOrderId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        });
    }

    private void handleRefundProcessed(JSONObject event) {
        JSONObject refundEntity = event.getJSONObject("payload")
                .getJSONObject("refund")
                .getJSONObject("entity");

        String razorpayRefundId = refundEntity.getString("id");

        refundRepository.findByRazorpayRefundId(razorpayRefundId).ifPresent(refund -> {
            refund.setStatus(RefundStatus.REFUNDED);
            refundRepository.save(refund);
        });
    }
}
