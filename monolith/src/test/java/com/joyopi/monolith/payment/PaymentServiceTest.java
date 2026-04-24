package com.joyopi.monolith.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.joyopi.monolith.payment.domain.PaymentStatus;
import com.joyopi.monolith.payment.dto.PaymentCommand;
import com.joyopi.monolith.payment.dto.PaymentResponse;
import com.joyopi.monolith.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 서비스 기능 테스트
 */
@SpringBootTest
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    @DisplayName("외부 API 시뮬레이션을 통해 결제가 성공적으로 처리된다.")
    void processPaymentSuccess() {
        // given
        PaymentCommand command = PaymentCommand.builder()
                .orderId(100L)
                .amount(50000L)
                .build();

        // when
        PaymentResponse response = paymentService.pay(command);

        // then
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }
}
