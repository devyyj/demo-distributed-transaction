package com.joyopi.monolith.payment.service;

import com.joyopi.monolith.payment.domain.Payment;
import com.joyopi.monolith.payment.domain.PaymentStatus;
import com.joyopi.monolith.payment.dto.PaymentCommand;
import com.joyopi.monolith.payment.dto.PaymentResponse;
import com.joyopi.monolith.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 비즈니스 로직 및 외부 API 시뮬레이션을 처리하는 서비스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 결제를 진행합니다.
     */
    @Transactional
    public PaymentResponse pay(PaymentCommand command) {
        // [의도적 실패 코드] 결제 실패 상태를 시뮬레이션하려면 아래 주석을 해제하세요.
        // if (command.getAmount() > 0) return PaymentResponse.builder().status(PaymentStatus.FAIL).build();

        Payment payment = paymentRepository.save(Payment.builder()
                .orderId(command.getOrderId())
                .amount(command.getAmount())
                .build());

        try {
            simulateExternalApi();
            payment.complete();
            log.info("Payment success for order: {}", command.getOrderId());
            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .status(PaymentStatus.SUCCESS)
                    .build();
        } catch (Exception e) {
            payment.fail();
            log.error("Payment failed for order: {}", command.getOrderId(), e);
            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .status(PaymentStatus.FAIL)
                    .build();
        }
    }

    private void simulateExternalApi() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("결제 처리 중 중단되었습니다.");
        }
    }
}
