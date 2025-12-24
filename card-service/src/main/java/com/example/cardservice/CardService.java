package com.example.cardservice;

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
     * 카드 승인을 위해 필요한 정보와 실패 시 보상을 위해 넘겨줄 정보를 포함
     */
    public record PointDeductedEvent(Long orderId, Long userId, int pointAmount, int cardAmount) {
    }

    /**
     * 성공/실패 통보를 위한 DTO
     */
    public record OrderResult(Long orderId, Long userId, int pointAmount) {
    }

    @Bean
    public Consumer<PointDeductedEvent> pointDeductedCOnsumer() {
        return event -> {
            log.info("카드 결제 승인 요청 : {}원", event.cardAmount());
            OrderResult data = new OrderResult(event.orderId, event.userId, event.pointAmount);
            try {
                callExternalCardApi(event.cardAmount());
                log.info("카드 승인 완료: {}원. 최종 완료 이벤트 발행.", event.cardAmount());
                streamBridge.send("cardApproved-out-0", data);
            } catch (Exception e) {
                log.error("카드 승인 실패: {}. 보상 트랜잭션 트리거.", e.getMessage());
                streamBridge.send("cardFailed-out-0", data);
            }
        };
    }

    private void callExternalCardApi(int amount) {
        try {
            // [1] 결제 한도 검증 (100만원 초과 시 예외 발생)
            if (amount > 1_000_000) {
                log.warn("카드 결제 승인 거절: 한도 초과 (요청 금액: {}원)", amount);
                // RuntimeException은 스프링의 @Transactional을 만나면 자동으로 롤백을 수행합니다.
                throw new RuntimeException("카드사 결제 한도(100만원)를 초과했습니다.");
            }
            // 실제 외부 API 호출과 유사한 환경을 위해 1초 대기
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("네트워크 지연 시뮬레이션 중 오류 발생", e);
            Thread.currentThread().interrupt();
        }
    }
}