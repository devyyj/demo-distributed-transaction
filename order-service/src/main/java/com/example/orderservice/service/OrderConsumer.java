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

    // 중첩된 공통 이벤트 구조
    public record DomainEvent(String type, EventPayload payload) {
        public record EventPayload(Long orderId, String reason) {}
    }

    /**
     * 카드 도메인 이벤트 수신 및 분기 처리
     */
    @Bean
    public Consumer<DomainEvent> cardEventsConsumer() {
        return event -> {
            DomainEvent.EventPayload data = event.payload();
            if("CardCompleted".equals(event.type())) {
                log.info("최종 결제 성공 수신: 주문 ID {}", data.orderId());
                orderService.updateStatus(data.orderId(), OrderStatus.COMPLETED);
            } else if("CardFailed".equals(event.type())) {
                log.warn("카드 결제 실패 수신 - 주문 ID {}: 사유 {}", data.orderId(), data.reason());
                orderService.updateStatus(data.orderId(), OrderStatus.FAILED);
            }
        };
    }

    /**
     * 포인트 도메인 이벤트 수신 및 분기 처리
     */
    @Bean
    public Consumer<DomainEvent> pointEventsConsumer() {
        return event -> {
            DomainEvent.EventPayload data = event.payload();
            if ("PointFailed".equals(event.type())) {
                log.warn("포인트 차감 실패 수신 - 주문 ID {}: 사유 {}", data.orderId(), data.reason());
                orderService.updateStatus(data.orderId(), OrderStatus.FAILED);
            }
        };
    }
}