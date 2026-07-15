package com.payKaroo.payment_service.controller;

import com.payKaroo.payment_service.dto.CreateOrderRequest;
import com.payKaroo.payment_service.dto.CreateOrderResponse;
import com.payKaroo.payment_service.dto.VerifyPaymentRequest;
import com.payKaroo.payment_service.entity.Payment;
import com.payKaroo.payment_service.entity.PaymentStatus;
import com.payKaroo.payment_service.service.PaymentService;
import com.payKaroo.payment_service.service.RedisService;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final RedisService redisService;

    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final long IDEMPOTENCY_TTL_HOURS = 24;

    public PaymentController(PaymentService paymentService, RedisService redisService) {
        this.paymentService = paymentService;
        this.redisService = redisService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request)
            throws RazorpayException {

        String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;
        if (redisService.exists(redisKey)) {
            CreateOrderResponse cachedResponse = (CreateOrderResponse) redisService.get(redisKey);
            return ResponseEntity.ok(cachedResponse);
        }

        CreateOrderResponse response = paymentService.createOrder(request);
        redisService.save(redisKey, response, IDEMPOTENCY_TTL_HOURS);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        String result = paymentService.verifyPayment(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<Payment>> getPaymentHistory(
            @RequestParam Long userId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Payment> payments = paymentService.getPaymentHistory(userId, status, page, size);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        paymentService.processWebhook(payload, signature);
        return ResponseEntity.ok("Webhook processed");
    }
}
