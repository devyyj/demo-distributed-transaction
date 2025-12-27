package com.example.pointservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * 포인트 이벤트 컨슈머
 * 중첩된 JSON 구조(payload, type)에 맞춰 DTO를 정의하고 로직을 수행합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PointConsumer {

    private final PointService pointService;

    // 루트 레벨의 type과 payload 객체를 매핑하기 위한 구조
    public record OrderEvent(String type, OrderPayload payload) {
        // 실제 비즈니스 데이터가 담긴 payload 내부 객체
        public record OrderPayload(Long orderId, Long userId, int pointAmount, int cardAmount) {}
    }

    public record CardEvent(String type, CardPayload payload) {
        public record CardPayload(Long orderId, Long userId, int pointAmount, String reason) {}
    }

    /**
     * 주문 도메인 이벤트 수신
     */
    @Bean
    public Consumer<OrderEvent> orderEventsConsumer() {
        return event -> {
            // 루트 레벨의 type 필드로 분기 처리
            if ("OrderCreated".equals(event.type())) {
                OrderEvent.OrderPayload data = event.payload();
                log.info("이벤트 수신: 주문 생성 (OrderId: {})", data.orderId());
                // 내부 payload 객체의 데이터를 서비스 로직에 전달
                pointService.deduct(data.orderId(), data.userId(), data.pointAmount(), data.cardAmount());
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
                CardEvent.CardPayload data = event.payload();
                log.info("이벤트 수신: 카드 결제 실패 (주문 ID: {}), 보상 트랜잭션 실행", data.orderId());
                pointService.restore(data.userId(), data.pointAmount(), data.orderId());
            }
        };
    }
}