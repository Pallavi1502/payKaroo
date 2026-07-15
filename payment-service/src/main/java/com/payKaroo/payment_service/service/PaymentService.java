package com.payKaroo.payment_service.service;


import com.payKaroo.payment_service.dto.CreateOrderRequest;
import com.payKaroo.payment_service.dto.CreateOrderResponse;
import com.payKaroo.payment_service.dto.VerifyPaymentRequest;
import com.payKaroo.payment_service.entity.Payment;
import com.payKaroo.payment_service.entity.PaymentStatus;
import com.payKaroo.payment_service.exception.PaymentNotFoundException;
import com.payKaroo.payment_service.exception.PaymentVerificationException;
import com.payKaroo.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${razorpay.key-secret}")
    private String keySecret;

    public PaymentService(RazorpayClient razorpayClient, PaymentRepository paymentRepository) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
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
            throw new PaymentVerificationException("Payment signature verification failed");
        }

        payment.setPaymentId(request.getRazorpayPaymentId());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

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
}
