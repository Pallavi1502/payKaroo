package com.payKaroo.payment_service.repository;

import com.payKaroo.payment_service.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
