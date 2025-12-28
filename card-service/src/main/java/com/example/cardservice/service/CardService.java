package com.example.cardservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

/**
 * 카드 결제 비즈니스 로직을 담당하는 서비스 클래스입니다.
 * Resilience4j 서킷 브레이커가 적용되어 외부 시스템 장애에 대응합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final StreamBridge streamBridge;

    /**
     * 포인트 이벤트 구조 정의
     */
    public record PointEvent(String type, PointPayload payload) {
        public record PointPayload(Long orderId, Long userId, int pointAmount, int cardAmount) {
        }
    }

    /**
     * 카드 도메인 이벤트 구조 정의
     */
    public record CardEvent(String type, CardPayload payload) {
        public record CardPayload(Long orderId, Long userId, int pointAmount, String reason) {
        }
    }

    /**
     * 실제 카드 승인 처리를 수행합니다.
     *
     * @CircuitBreaker 어노테이션을 통해 장애 발생 시 fallbackMethod를 호출합니다.
     */
    @CircuitBreaker(name = "cardApprovalbreaker", fallbackMethod = "fallbackCardApproval")
    public void processCardApproval(PointEvent.PointPayload data) {
        log.info("서비스: 외부 카드사 API 호출 시도 - 주문 ID: {}", data.orderId());

        // 외부 API 호출 시뮬레이션
        callCardApi(data.cardAmount());

        // 승인 성공 시 성공 이벤트 발행
        streamBridge.send("cardEvents-out-0",
                new CardEvent("CardCompleted", new CardEvent.CardPayload(data.orderId(), null, 0, null)));

        log.info("서비스: 카드 승인 성공 완료 - 주문 ID: {}", data.orderId());
    }

    /**
     * 서킷 브레이커가 작동하거나 에러가 발생했을 때 호출되는 대체(Fallback) 로직입니다.
     * 이 메서드가 정상적으로 리턴되면 컨슈머는 메시지 처리가 완료된 것으로 간주하여 재시도를 하지 않습니다.
     */
    public void fallbackCardApproval(PointEvent.PointPayload data, Throwable t) {
        log.error("서비스: 서킷 브레이커/장애 발생으로 Fallback 실행 - 주문 ID: {}, 사유: {}", data.orderId(), t.getMessage());

        // 실패 이벤트 발행을 통해 Saga 보상 트랜잭션 유도
        streamBridge.send("cardEvents-out-0",
                new CardEvent("CardFailed",
                        new CardEvent.CardPayload(data.orderId(), data.userId(), data.pointAmount(), "카드 결제 처리 불가: " + t.getMessage())));

        log.info("서비스: 실패 이벤트 발행 완료. 컨슈머 재시도를 방지합니다.");
    }

    /**
     * 외부 카드사 API 호출 시뮬레이션 메서드
     */
    private void callCardApi(int amount) {
        // 테스트용: 100만원 초과 시 장애 발생 시뮬레이션
        if (amount > 1_000_000) {
            throw new RuntimeException("외부 카드사 시스템 응답 지연 및 장애");
        }

        try {
            if (amount == 123_456) Thread.sleep(3000);
            else Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}