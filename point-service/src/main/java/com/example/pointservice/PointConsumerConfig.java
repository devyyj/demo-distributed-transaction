package com.example.pointservice;

import com.example.pointservice.dto.OrderCreatedEvent;
import com.example.pointservice.dto.OrderFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PointConsumerConfig {

    private final PointService pointService;

    /**
     * 주문 생성 이벤트 수신 (Consumer)
     * - 메시징 인프라와 비즈니스 로직을 연결하는 어댑터 역할만 수행합니다.
     */
    @Bean
    public Consumer<OrderCreatedEvent> orderCreatedConsumer() {
        return event -> {
            log.info("Kafka 메시지 수신: 주문 생성 이벤트 (OrderId: {})", event.orderId());
            pointService.processOrder(event);
        };
    }

    /**
     * 보상 트랜잭션 수신 (Consumer)
     */
    @Bean
    public Consumer<OrderFailedEvent> compensationConsumer() {
        return event -> {
            log.info("Kafka 메시지 수신: 보상 트랜잭션 (UserId: {})", event.userId());
            pointService.compensate(event);
        };
    }
}