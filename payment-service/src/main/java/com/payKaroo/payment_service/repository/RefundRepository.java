package com.payKaroo.payment_service.repository;

import com.payKaroo.payment_service.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByRazorpayRefundId(String razorpayRefundId);
}
