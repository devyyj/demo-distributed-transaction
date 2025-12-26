package com.example.pointservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * 포인트 이벤트 컨슈머
 * - 외부 메시지(Kafka) 수신 및 서비스 로직 호출을 담당합니다.
 * - PointService와 분리되어 있으므로 프록시를 통한 @Transactional이 정상 작동합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PointConsumer {

    private final PointService pointService;

    public record OrderEvent(String type, Long orderId, Long userId, int pointAmount, int cardAmount) {
    }

    public record CardEvent(String type, Long orderId, Long userId, int pointAmount, String reason) {
    }

    /**
     * 주문 도메인 이벤트 수신
     */
    @Bean
    public Consumer<OrderEvent> orderEventsConsumer() {
        return event -> {
            if ("OrderCreated".equals(event.type())) {
                log.info("이벤트 수신: 주문 생성 (OrderId: {})", event.orderId());
                pointService.deduct(event.orderId(), event.userId(), event.pointAmount(), event.cardAmount());
            }
        };
    }

    /**
     * 카드 도메인 이벤트 수신 (보상 트랜잭션)
     */
    @Bean
    public Consumer<CardEvent> cardEventsConsumer() {
        return event -> {
            if ("CardFailed".equals(event.type())) {
                log.info("이벤트 수신: 카드 결제 실패 (주문 ID: {}), 보상 트랜잭션 실행", event.orderId());
                pointService.restore(event.userId(), event.pointAmount());
            }
        };
    }
}