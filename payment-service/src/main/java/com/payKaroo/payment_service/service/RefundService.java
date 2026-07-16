package com.payKaroo.payment_service.service;

import com.payKaroo.payment_service.dto.RefundRequest;
import com.payKaroo.payment_service.dto.RefundResponse;
import com.payKaroo.payment_service.entity.Payment;
import com.payKaroo.payment_service.entity.Refund;
import com.payKaroo.payment_service.entity.RefundStatus;
import com.payKaroo.payment_service.event.RefundInitiatedEvent;
import com.payKaroo.payment_service.exception.PaymentNotFoundException;
import com.payKaroo.payment_service.kafka.PaymentEventProducer;
import com.payKaroo.payment_service.repository.PaymentRepository;
import com.payKaroo.payment_service.repository.RefundRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class RefundService {
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final PaymentEventProducer eventProducer;

    public RefundService(RefundRepository refundRepository, PaymentRepository paymentRepository,
                         RazorpayClient razorpayClient, PaymentEventProducer eventProducer) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
        this.eventProducer = eventProducer;
    }


    public RefundResponse initiateRefund(RefundRequest request) throws RazorpayException{
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment found with ID: " + request.getPaymentId()));

        if (payment.getPaymentId() == null) {
            throw new IllegalStateException("Cannot refund a payment that was never completed");
        }

        int amountInSmallestUnit = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        JSONObject refundRequestBody = new JSONObject();
        refundRequestBody.put("amount", amountInSmallestUnit);

        com.razorpay.Refund razorpayRefund = razorpayClient.payments.refund(
                payment.getPaymentId(), refundRequestBody);

        String razorpayRefundId = razorpayRefund.get("id");

        Refund refund = new Refund();
        refund.setPaymentId(payment.getId());
        refund.setAmount(request.getAmount());
        refund.setReason(request.getReason());
        refund.setRazorpayRefundId(razorpayRefundId);
        refund.setStatus(RefundStatus.REFUNDED);

        Refund savedRefund = refundRepository.save(refund);

        eventProducer.publishRefundInitiated(new RefundInitiatedEvent(
                payment.getUserId(), "test@example.com", savedRefund.getId(), payment.getId(), savedRefund.getAmount()));

        return new RefundResponse(
                savedRefund.getId(),
                savedRefund.getPaymentId(),
                savedRefund.getAmount(),
                savedRefund.getStatus().name(),
                savedRefund.getRazorpayRefundId()
        );
    }


    public RefundResponse getRefundStatus(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentNotFoundException("No refund found with ID: " + refundId));

        return new RefundResponse(
                refund.getId(),
                refund.getPaymentId(),
                refund.getAmount(),
                refund.getStatus().name(),
                refund.getRazorpayRefundId()
        );
    }

}
