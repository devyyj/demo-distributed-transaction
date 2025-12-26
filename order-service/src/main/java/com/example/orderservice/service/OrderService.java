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

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final StreamBridge streamBridge;
    private final OrderRepository orderRepository;

    // DTO 정의 (내부 record)
    public record OrderCreatedEvent(Long orderId, Long userId, int pointAmount, int cardAmount) {}
    public record CardCompletedEvent(Long orderId) {}
    public record CardFailedEvent(Long orderId, Long userId, int pointAmount, String reason) {}
    public record PointFailedEvent(Long orderId, String reason) {}

    /**
     * 주문 생성 및 Saga 시작
     * 발행 토픽: order.created
     */
    @Transactional
    public void processOrder(Long userId, int totalAmount, int pointAmount) {
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .pointAmount(pointAmount)
                .build();
        orderRepository.save(order);

        log.info("Saga 시작 - 주문 생성(PENDING): {}", order.getId());

        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), userId, pointAmount, order.getCardAmount());
        streamBridge.send("orderCreated-out-0", event);
    }

    /**
     * 최종 성공 수신 (카드 승인 완료)
     * 구독 토픽: card.completed
     */
    @Bean
    public Consumer<CardCompletedEvent> cardCompletedConsumer() {
        return event -> {
            log.info("최종 결제 성공 수신: 주문 ID {}", event.orderId());
            updateStatus(event.orderId(), OrderStatus.COMPLETED);
        };
    }

    /**
     * 카드 결제 실패 수신 (주문 실패 처리)
     * 구독 토픽: card.failed
     */
    @Bean
    public Consumer<CardFailedEvent> cardFailedConsumer() {
        return event -> {
            log.warn("카드 결제 실패 수신 - 주문 ID {}: 사유 {}", event.orderId(), event.reason());
            updateStatus(event.orderId(), OrderStatus.FAILED);
        };
    }

    /**
     * 포인트 차감 실패 수신 (주문 실패 처리)
     * 구독 토픽: point.failed
     */
    @Bean
    public Consumer<PointFailedEvent> pointFailedConsumer() {
        return event -> {
            log.warn("포인트 차감 실패 수신 - 주문 ID {}: 사유 {}", event.orderId(), event.reason());
            updateStatus(event.orderId(), OrderStatus.FAILED);
        };
    }

    protected void updateStatus(Long orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
            log.info("주문 상태 변경 완료: {}", status);
        });
    }
}