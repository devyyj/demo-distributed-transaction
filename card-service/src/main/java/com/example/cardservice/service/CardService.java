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

    // type 필드를 포함한 이벤트 DTO
    public record PointEvent(String type, Long orderId, Long userId, int pointAmount, int cardAmount) {}
    public record CardEvent(String type, Long orderId, Long userId, int pointAmount, String reason) {}

    @Bean
    public Consumer<PointEvent> pointEventsConsumer() {
        return event -> {
            if (!"PointCompleted".equals(event.type())) return;

            log.info("카드 승인 요청 - 금액: {}원", event.cardAmount());
            try {
                callCardApi(event.cardAmount());
                // 성공 이벤트 발행
                streamBridge.send("cardEvents-out-0", new CardEvent("CardCompleted", event.orderId(), null, 0, null));
            } catch (Exception e) {
                log.error("카드 승인 실패 - 사유: {}", e.getMessage());
                // 실패 이벤트 발행
                streamBridge.send("cardEvents-out-0", new CardEvent("CardFailed", event.orderId(), event.userId(), event.pointAmount(), e.getMessage()));
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