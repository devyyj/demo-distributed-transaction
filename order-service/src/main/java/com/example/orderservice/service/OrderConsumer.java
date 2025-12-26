package com.example.orderservice.service;

import com.example.orderservice.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class OrderConsumer {
    private final OrderService orderService;


    public record CardCompletedEvent(Long orderId) {
    }

    public record CardFailedEvent(Long orderId, Long userId, int pointAmount, String reason) {
    }

    public record PointFailedEvent(Long orderId, String reason) {
    }

    /**
     * 최종 성공 수신 (카드 승인 완료)
     * 구독 토픽: card.completed
     */
    @Bean
    public Consumer<CardCompletedEvent> cardCompletedConsumer() {
        return event -> {
            log.info("최종 결제 성공 수신: 주문 ID {}", event.orderId());
            orderService.updateStatus(event.orderId(), OrderStatus.COMPLETED);
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
            orderService.updateStatus(event.orderId(), OrderStatus.FAILED);
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
            orderService.updateStatus(event.orderId(), OrderStatus.FAILED);
        };
    }
}
