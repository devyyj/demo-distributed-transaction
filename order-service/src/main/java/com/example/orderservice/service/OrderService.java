package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final StreamBridge streamBridge;
    private final RestTemplate restTemplate = new RestTemplate();
    private final OrderRepository orderRepository;

    /**
     * 포인트/카드 서비스로 전달할 생성 이벤트 DTO
     * 주문 서비스는 모든 금액 정보를 알고 있으므로 전체를 포함함
     */
    public record OrderCreatedEvent(Long orderId, Long userId, int pointAmount, int cardAmount) {
    }

    /**
     * 외부 서비스로부터 결과를 받을 때 사용하는 DTO
     * 상태 업데이트를 위해 orderId만 있으면 됨
     */
    public record OrderResultEvent(Long orderId) {
    }

    @Transactional
    public void processOrder(Long userId, int totalAmount, int pointAmount) {
        // 주문 생성
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .pointAmount(pointAmount)
                .build();
        orderRepository.save(order);

        log.info("결제 주문 생성 완료 (PENDING) : {}", order.getId());

        // 포인트 이벤트 발행
        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), userId, pointAmount, order.getCardAmount());
        streamBridge.send("orderCreated-out-0", event);

    }

    // 최종 성공 이벤트 소비
    @Bean
    public Consumer<OrderResultEvent> orderCompletedConsumer() {
        return event -> {
            log.info("최종 결제 성공 이벤트 수신: 주문 ID {}", event.orderId());
            updateStatus(event.orderId(), OrderStatus.COMPLETED);
        };
    }


    // 최종 실패 이벤트 소비
    @Bean
    public Consumer<OrderResultEvent> orderFailedConsumer() {
        return event -> {
            log.warn("최종 결제 실패 이벤트 수신: 주문 ID {}", event.orderId());
            updateStatus(event.orderId(), OrderStatus.FAILED);
        };
    }

    private void updateStatus(Long orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
    }
}
