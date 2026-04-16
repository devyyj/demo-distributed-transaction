package com.joyopi.monolith.order.service;

import com.joyopi.monolith.order.domain.Order;
import com.joyopi.monolith.order.domain.OrderStatus;
import com.joyopi.monolith.order.dto.OrderRequest;
import com.joyopi.monolith.order.dto.OrderResponse;
import com.joyopi.monolith.order.repository.OrderRepository;
import com.joyopi.monolith.payment.service.PaymentService;
import com.joyopi.monolith.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PointService pointService;
    private final PaymentService paymentService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("주문 생성 시작 - userId: {}, productId: {}, totalAmount: {}, pointAmount: {}",
                request.userId(), request.productId(), request.totalAmount(), request.pointAmount());

        Order order = orderRepository.save(Order.builder()
                .userId(request.userId())
                .productId(request.productId())
                .totalAmount(request.totalAmount())
                .pointAmount(request.pointAmount())
                .status(OrderStatus.PENDING)
                .build());

        log.info("주문 생성 완료 (PENDING) - orderId: {}", order.getId());

        // [실패 케이스] 주문 생성 직후 강제 실패 시뮬레이션
        // throw new RuntimeException("주문 생성 후 강제 실패 - orderId: " + order.getId());

        // 포인트 차감
        pointService.usePoints(request.userId(), request.pointAmount());

        // 결제 처리 (결제 금액 = 총액 - 포인트 사용액)
        int paymentAmount = request.totalAmount() - request.pointAmount();
        paymentService.processPayment(order.getId(), paymentAmount);

        order.complete();
        log.info("주문 처리 완료 (COMPLETED) - orderId: {}", order.getId());

        return OrderResponse.from(order);
    }
}
