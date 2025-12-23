package com.example.cardservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CardService {

    /**
     * 카드 승인 요청
     * 결제 금액이 1,000만원을 초과하면 예외를 발생시켜 트랜잭션 롤백을 유도합니다.
     */
    public void approve(int amount) {
        // [1] 결제 한도 검증 (1,000만원 초과 시 예외 발생)
        if (amount > 10_000_000) {
            log.warn("카드 결제 승인 거절: 한도 초과 (요청 금액: {}원)", amount);
            // RuntimeException은 스프링의 @Transactional을 만나면 자동으로 롤백을 수행합니다.
            throw new RuntimeException("카드사 결제 한도(1,000만원)를 초과했습니다.");
        }

        log.info("카드 결제 승인 요청 중: {}원", amount);

        // [2] 네트워크 지연 시뮬레이션
        simulateNetworkDelay();

        log.info("카드 승인 완료: {}원", amount);
    }

    private void simulateNetworkDelay() {
        try {
            // 실제 외부 API 호출과 유사한 환경을 위해 1초 대기
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("네트워크 지연 시뮬레이션 중 오류 발생", e);
            Thread.currentThread().interrupt();
        }
    }
}