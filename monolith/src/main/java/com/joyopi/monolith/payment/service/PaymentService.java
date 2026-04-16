package com.joyopi.monolith.payment.service;

import com.joyopi.monolith.common.exception.PaymentProcessException;
import com.joyopi.monolith.payment.domain.Payment;
import com.joyopi.monolith.payment.domain.PaymentStatus;
import com.joyopi.monolith.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${payment.simulate-delay-ms}")
    private int simulateDelayMs;

    @Transactional
    public void processPayment(Long orderId, int amount) {
        log.info("결제 시작 - orderId: {}, amount: {}", orderId, amount);

        Payment payment = paymentRepository.save(Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .status(PaymentStatus.READY)
                .build());

        // 외부 결제 API 호출 시뮬레이션 (딜레이 포함)
        simulateExternalPayment(orderId, amount);

        // [실패 케이스] 결제 실패 시뮬레이션
        // payment.fail();
        // throw new PaymentProcessException(orderId, "외부 결제 API 호출 실패");

        payment.succeed();
        log.info("결제 완료 - orderId: {}, paymentId: {}", orderId, payment.getId());
    }

    private void simulateExternalPayment(Long orderId, int amount) {
        try {
            log.info("외부 결제 API 호출 시작 - orderId: {}, amount: {}", orderId, amount);
            Thread.sleep(simulateDelayMs);
            log.info("외부 결제 API 호출 완료 - orderId: {}, amount: {}", orderId, amount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentProcessException(orderId, "결제 처리 중 인터럽트 발생");
        }
    }
}
