package com.joyopi.monolith.payment.repository;

import com.joyopi.monolith.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
