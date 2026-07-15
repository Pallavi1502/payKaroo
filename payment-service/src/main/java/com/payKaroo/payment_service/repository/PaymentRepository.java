package com.payKaroo.payment_service.repository;

import com.payKaroo.payment_service.entity.Payment;
import com.payKaroo.payment_service.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);

    Page<Payment> findByUserId(Long userId, Pageable pageable);
    Page<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status, Pageable pageable);
}
