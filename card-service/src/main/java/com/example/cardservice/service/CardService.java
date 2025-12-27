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

    /**
     * 수신용: 포인트 이벤트 (중첩 구조)
     */
    public record PointEvent(String type, PointPayload payload) {
        public record PointPayload(Long orderId, Long userId, int pointAmount, int cardAmount) {}
    }

    /**
     * 송신용: 카드 이벤트 (중첩 구조로 수정)
     * Debezium Outbox 패턴의 결과물과 형식을 맞추어 컨슈머가 일관되게 처리할 수 있도록 합니다.
     */
    public record CardEvent(String type, CardPayload payload) {
        public record CardPayload(Long orderId, Long userId, int pointAmount, String reason) {}
    }

    @Bean
    public Consumer<PointEvent> pointEventsConsumer() {
        return event -> {
            // 루트 레벨의 type 필드로 이벤트 종류 확인
            if (!"PointCompleted".equals(event.type())) return;

            PointEvent.PointPayload data = event.payload();
            log.info("카드 승인 요청 - 금액: {}원", data.cardAmount());
            try {
                callCardApi(data.cardAmount());
                // 성공 이벤트 발행 (중첩 구조 적용)
                streamBridge.send("cardEvents-out-0",
                        new CardEvent("CardCompleted", new CardEvent.CardPayload(data.orderId(), null, 0, null)));
                log.info("카드 승인 성공 및 이벤트 발행 : {}", data.orderId());
            } catch (Exception e) {
                log.error("카드 승인 실패 - 사유: {}", e.getMessage());
                // 실패 이벤트 발행 (중첩 구조 적용)
                streamBridge.send("cardEvents-out-0",
                        new CardEvent("CardFailed", new CardEvent.CardPayload(data.orderId(), data.userId(), data.pointAmount(), e.getMessage())));
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