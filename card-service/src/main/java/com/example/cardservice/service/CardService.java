package com.example.cardservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final StreamBridge streamBridge;

    // 개선된 DTO 정의
    public record PointCompletedEvent(Long orderId, Long userId, int pointAmount, int cardAmount) {}
    public record CardCompletedEvent(Long orderId) {}
    public record CardFailedEvent(Long orderId, Long userId, int pointAmount, String reason) {}

    /**
     * [구독] 포인트 차감 성공 후 카드 결제 승인
     * 토픽: point.completed
     */
    @Bean
    public Consumer<PointCompletedEvent> pointCompletedConsumer() {
        return event -> {
            log.info("카드 승인 요청 - 금액: {}원", event.cardAmount());
            try {
                callCardApi(event.cardAmount());
                // 성공 시 card.completed 발행
                log.info("카드 승인 성공 - ID: {}", event.orderId());
                streamBridge.send("cardCompleted-out-0", new CardCompletedEvent(event.orderId()));
            } catch (Exception e) {
                log.error("카드 승인 실패 - 사유: {}", e.getMessage());
                // 실패 시 card.failed 발행 (보상 트랜잭션 트리거)
                CardFailedEvent failureEvent = new CardFailedEvent(
                        event.orderId(), event.userId(), event.pointAmount(), e.getMessage()
                );
                streamBridge.send("cardFailed-out-0", failureEvent);
            }
        };
    }

    private void callCardApi(int amount) {
        if (amount > 1_000_000) {
            throw new RuntimeException("카드사 한도 초과");
        }
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }
}