package com.joyopi.monolith.order.service;

import com.joyopi.monolith.order.domain.Order;
import com.joyopi.monolith.order.dto.OrderCreateCommand;
import com.joyopi.monolith.order.dto.OrderResponse;
import com.joyopi.monolith.order.repository.OrderRepository;
import com.joyopi.monolith.payment.domain.PaymentStatus;
import com.joyopi.monolith.payment.dto.PaymentCommand;
import com.joyopi.monolith.payment.dto.PaymentResponse;
import com.joyopi.monolith.payment.service.PaymentService;
import com.joyopi.monolith.point.dto.PointUseCommand;
import com.joyopi.monolith.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 생성 프로세스를 조정하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PointService pointService;
    private final PaymentService paymentService;

    /**
     * 주문을 생성합니다. (포인트 차감 -> 결제 -> 주문 완료)
     */
    @Transactional
    public OrderResponse createOrder(OrderCreateCommand command) {
        // [의도적 실패 코드] 시스템 오류를 발생시키려면 아래 주석을 해제하세요.
        // if (command != null) throw new RuntimeException("의도적인 시스템 오류");

        // 1. 주문 초기 생성 (PENDING)
        Order order = orderRepository.save(Order.builder()
                .userId(command.getUserId())
                .productId(command.getProductId())
                .totalAmount(command.getTotalAmount())
                .pointAmount(command.getPointAmount())
                .build());

        // 2. 포인트 사용 요청 (하위 레이어 Command 변환)
        pointService.usePoint(PointUseCommand.builder()
                .userId(command.getUserId())
                .amount(command.getPointAmount())
                .build());

        // 3. 결제 요청 (하위 레이어 Command 변환)
        Long payAmount = command.getTotalAmount() - command.getPointAmount();
        PaymentResponse paymentResponse = paymentService.pay(PaymentCommand.builder()
                .orderId(order.getId())
                .amount(payAmount)
                .build());

        // 4. 주문 완료 처리
        if (paymentResponse.getStatus() == PaymentStatus.SUCCESS) {
            order.complete();
        } else {
            order.cancel();
        }

        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .build();
    }
}
