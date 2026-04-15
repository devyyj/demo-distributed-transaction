package com.joyopi.monolith.order.service;

import com.joyopi.monolith.order.domain.Order;
import com.joyopi.monolith.order.domain.OrderStatus;
import com.joyopi.monolith.order.dto.OrderRequest;
import com.joyopi.monolith.order.dto.OrderResponse;
import com.joyopi.monolith.order.repository.OrderRepository;
import com.joyopi.monolith.payment.service.PaymentService;
import com.joyopi.monolith.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PointService pointService;
    private final PaymentService paymentService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Order order = orderRepository.save(Order.builder()
                .userId(request.userId())
                .productId(request.productId())
                .totalAmount(request.totalAmount())
                .pointAmount(request.pointAmount())
                .status(OrderStatus.PENDING)
                .build());

        // 포인트 차감
        pointService.usePoints(request.userId(), request.pointAmount());

        // 결제 처리 (결제 금액 = 총액 - 포인트 사용액)
        int paymentAmount = request.totalAmount() - request.pointAmount();
        paymentService.processPayment(order.getId(), paymentAmount);

        order.complete();
        return OrderResponse.from(order);
    }
}
