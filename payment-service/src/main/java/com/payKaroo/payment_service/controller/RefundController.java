package com.payKaroo.payment_service.controller;

import com.payKaroo.payment_service.dto.RefundRequest;
import com.payKaroo.payment_service.dto.RefundResponse;
import com.payKaroo.payment_service.service.RefundService;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<RefundResponse> initiateRefund(@Valid @RequestBody RefundRequest request) throws RazorpayException {
        RefundResponse response = refundService.initiateRefund(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponse> getRefundStatus(@PathVariable Long refundId) {
        RefundResponse response = refundService.getRefundStatus(refundId);
        return ResponseEntity.ok(response);
    }

}
