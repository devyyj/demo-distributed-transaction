package com.example.cardservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * 카프카 이벤트를 수신하는 컨슈머 클래스입니다.
 * 서비스 클래스와 분리하여 서킷 브레이커 프록시가 정상 동작하도록 설계되었습니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CardConsumer {

    private final CardService cardService;

    /**
     * 포인트 서비스로부터 결제 요청 이벤트를 수신합니다.
     */
    @Bean
    public Consumer<CardService.PointEvent> pointEventsConsumer() {
        return event -> {
            // 결제 완료 이벤트가 아닌 경우 무시
            if (!"PointCompleted".equals(event.type())) {
                return;
            }

            CardService.PointEvent.PointPayload data = event.payload();
            log.info("컨슈머: 카드 승인 요청 수신 - 주문 ID: {}", data.orderId());

            // 외부 클래스인 CardService의 메서드를 호출하므로 AOP 프록시가 적용됩니다.
            cardService.processCardApproval(data);
        };
    }
}