package com.payKaroo.payment_service.controller;

import com.payKaroo.payment_service.dto.CreateOrderRequest;
import com.payKaroo.payment_service.dto.CreateOrderResponse;
import com.payKaroo.payment_service.dto.VerifyPaymentRequest;
import com.payKaroo.payment_service.service.PaymentService;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) throws RazorpayException {
        CreateOrderResponse response = paymentService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        String result = paymentService.verifyPayment(request);
        return ResponseEntity.ok(result);
    }
}
