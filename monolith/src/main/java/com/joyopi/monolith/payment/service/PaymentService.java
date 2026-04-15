package com.joyopi.monolith.payment.service;

import com.joyopi.monolith.payment.domain.Payment;
import com.joyopi.monolith.payment.domain.PaymentStatus;
import com.joyopi.monolith.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void processPayment(Long orderId, int amount) {
        Payment payment = paymentRepository.save(Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .status(PaymentStatus.READY)
                .build());

        // 외부 결제 API 호출 모킹
        log.info("외부 결제 API 호출 - orderId: {}, amount: {}", orderId, amount);
        payment.succeed();
    }
}
