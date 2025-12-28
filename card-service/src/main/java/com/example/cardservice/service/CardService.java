package com.example.cardservice.service;

import com.example.cardservice.exception.CardBusinessException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

/**
 * 카드 결제 비즈니스 로직을 담당하는 서비스 클래스입니다.
 * 비즈니스 실패와 시스템 실패를 분리하여 서킷 브레이커를 효율적으로 관리합니다.
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
     * 카드 승인 처리를 수행합니다.
     * 비즈니스 예외(한도 초과 등)는 내부에서 처리하여 서킷 브레이커 카운트에서 제외하고,
     * 그 외의 런타임 예외는 서킷 브레이커가 감지하여 fallbackMethod를 호출합니다.
     */
    @CircuitBreaker(name = "cardApprovalbreaker", fallbackMethod = "fallbackCardApproval")
    public void processCardApproval(PointEvent.PointPayload data) {
        log.info("서비스: 카드 승인 프로세스 시작 - 주문 ID: {}", data.orderId());

        try {
            // 외부 API 호출 시뮬레이션
            callCardApi(data.cardAmount());

            // 승인 성공 시 성공 이벤트 발행
            streamBridge.send("cardEvents-out-0",
                    new CardEvent("CardCompleted", new CardEvent.CardPayload(data.orderId(), null, 0, null)));

            log.info("서비스: 카드 승인 성공 완료 - 주문 ID: {}", data.orderId());

        } catch (CardBusinessException e) {
            // [중요] 비즈니스 예외(한도 초과 등) 처리
            // 예외를 밖으로 던지지 않고 직접 실패 이벤트를 발행한 뒤 리턴합니다.
            // 이로써 서킷 브레이커는 이를 '성공'으로 간주하여 카운트하지 않습니다.
            log.warn("서비스: 비즈니스 예외 발생(서킷 미적용) - 주문 ID: {}, 사유: {}", data.orderId(), e.getMessage());

            sendFailureEvent(data, e.getMessage());
        }
    }

    /**
     * 시스템 장애(네트워크 오류, 타임아웃 등) 발생 시 호출되는 Fallback 로직입니다.
     */
    public void fallbackCardApproval(PointEvent.PointPayload data, Throwable t) {
        log.error("서비스: 시스템 장애로 인한 Fallback 실행 - 주문 ID: {}, 사유: {}", data.orderId(), t.getMessage());

        // 시스템 장애에 대한 실패 이벤트 발행
        sendFailureEvent(data, "시스템 오류로 인한 결제 불가: " + t.getMessage());
    }

    /**
     * 공통 실패 이벤트 발행 로직
     */
    private void sendFailureEvent(PointEvent.PointPayload data, String reason) {
        streamBridge.send("cardEvents-out-0",
                new CardEvent("CardFailed",
                        new CardEvent.CardPayload(data.orderId(), data.userId(), data.pointAmount(), reason)));
        log.info("서비스: 실패 이벤트 발행 완료(Saga 보상 트랜잭션 유도)");
    }

    /**
     * 외부 카드사 API 호출 시뮬레이션 메서드
     */
    private void callCardApi(int amount) {
        log.info("카드 결제 API 호출");
        // 1. 비즈니스 예외 상황: 한도 초과 (100만원 초과 시)
        if (amount > 1_000_000) {
            throw new CardBusinessException("카드 결제 한도 초과");
        }
        try {
            // 2. 시스템 예외 상황: 특정 금액(50만원)일 때 외부 시스템 장애 시뮬레이션
            if (amount == 123_456) {
                Thread.sleep(5000);
//            throw new RuntimeException("외부 카드사 서버 응답 없음 (Timeout)");
            }
            // 정상 처리 지연 시뮬레이션
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}