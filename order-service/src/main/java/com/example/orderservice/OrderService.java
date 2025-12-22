package com.example.orderservice;

import com.example.demo.card.CardService;
import com.example.demo.point.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final PointService pointService;
    private final CardService cardService;

    public void processOrder(Long userId, int totalAmount, int pointAmount) {

        // 주문 생성
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .pointAmount(pointAmount)
                .build();

        log.info("결제 주문 생성 완료 : {}", order);

        // 포인트 차감
        pointService.deductPoint(userId, pointAmount);

        // 외부 카드사 승인 요청
        cardService.approve(order.getCardAmount());

        // 모든 단계 성공 시 완료 상태로 업데이트
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        log.info("결제 완료");
    }
}
