package com.example.pointservice;

import com.example.pointservice.dto.OrderCreatedEvent;
import com.example.pointservice.dto.OrderFailedEvent;
import com.example.pointservice.dto.OrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PointService {
    private final PointRepository pointRepository;
    private final StreamBridge streamBridge;

    /**
     * 주문 처리 및 포인트 차감 로직
     * - ConsumerConfig에서 호출됩니다.
     */
    public void processOrder(OrderCreatedEvent event) {
        log.info("비즈니스 로직 실행: 사용자 {}, 포인트 {}", event.userId(), event.pointAmount());

        try {
            deductPoint(event.userId(), event.pointAmount());

            // 로직 성공 시 성공 이벤트 발행
            streamBridge.send("pointDeducted-out-0", event);
            log.info("이벤트 발행 성공: pointDeducted-out-0");

        } catch (Exception e) {
            log.error("포인트 차감 실패 및 보상 이벤트 발행: {}", e.getMessage());
            // 실패 시 실패 이벤트 발행 (Saga 패턴의 보상 트랜잭션 트리거)
            streamBridge.send("orderFailed-out-0", new OrderResult(event.orderId()));
        }
    }

    /**
     * 보상 트랜잭션 로직
     */
    public void compensate(OrderFailedEvent event) {
        log.info("보상 트랜잭션 실행: 포인트 복구 (사용자 {}, 포인트 {})", event.userId(), event.pointAmount());
        restorePoint(event.userId(), event.pointAmount());
    }

    // 내부 비즈니스 메서드 (private -> protected or private 유지)
    private void deductPoint(Long userId, int amount) {
        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (point.getBalance() < amount) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }

        point.use(amount);
        log.info("포인트 차감 완료: -{} 포인트", amount);
    }

    private void restorePoint(Long userId, int amount) {
        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        int balance = point.getBalance();
        point.setBalance(balance + amount);
        log.info("포인트 복구 완료: 기존 {}, 충전 {}, 현재 {}", balance, amount, point.getBalance());
    }
}