package com.example.pointservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
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
    private final StreamBridge streamBridge;

    // 개선된 DTO 정의
    public record OrderCreatedEvent(Long orderId, Long userId, int pointAmount, int cardAmount) {}
    public record PointCompletedEvent(Long orderId, Long userId, int pointAmount, int cardAmount) {}
    public record PointFailedEvent(Long orderId, String reason) {}
    public record CardFailedEvent(Long orderId, Long userId, int pointAmount, String reason) {}

    /**
     * 주문 생성 이벤트 수신
     * 토픽: order.created
     */
    @Bean
    public Consumer<OrderCreatedEvent> orderCreatedConsumer() {
        return event -> {
            log.info("이벤트 수신: 주문 생성 (OrderId: {})", event.orderId());
            try {
                // 서로 다른 클래스(빈) 호출이므로 트랜잭션 프록시가 정상 작동함
                pointService.deduct(event.userId(), event.pointAmount());

                // 성공 시 다음 단계 이벤트 발행
                PointCompletedEvent successEvent = new PointCompletedEvent(
                        event.orderId(), event.userId(), event.pointAmount(), event.cardAmount()
                );
                streamBridge.send("pointCompleted-out-0", successEvent);
            } catch (Exception e) {
                log.error("포인트 차감 실패: {}", e.getMessage());
                // 실패 시 주문 서비스에 알림
                streamBridge.send("pointFailed-out-0", new PointFailedEvent(event.orderId(), e.getMessage()));
            }
        };
    }

    /**
     * 카드 결제 실패 이벤트 수신 (보상 트랜잭션)
     * 토픽: card.failed
     */
    @Bean
    public Consumer<CardFailedEvent> cardFailedConsumer() {
        return event -> {
            log.info("이벤트 수신: 카드 결제 실패 (주문 ID: {}), 보상 트랜잭션 실행", event.orderId());
            pointService.restore(event.userId(), event.pointAmount());
        };
    }
}