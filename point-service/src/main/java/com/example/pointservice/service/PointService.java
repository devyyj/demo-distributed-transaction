package com.example.pointservice.service;

import com.example.pointservice.entity.OutboxEvent;
import com.example.pointservice.entity.Point;
import com.example.pointservice.repository.OutboxRepository;
import com.example.pointservice.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * 포인트 비즈니스 로직 서비스
 * - 데이터베이스 트랜잭션을 관리하며 엔티티의 상태 변경을 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final PointRepository pointRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    // 이벤트 DTO 정의
    public record PointCompletedEvent(Long orderId, Long userId, int pointAmount, int cardAmount) {
    }

    public record PointFailedEvent(Long orderId, String reason) {
    }

    /**
     * 포인트 차감 로직
     */
    public void deduct(Long orderId, Long userId, int amount, int cardAmount) {
        try {

            Point point = pointRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 엔티티 상태 변경 (Dirty Checking에 의해 save() 호출 없이 DB 반영)
            point.use(amount);
            log.info("포인트 차감 완료 - 사용자: {}, 차감액: {}, 현재 잔액: {}", userId, amount, point.getBalance());

            // 성공 이벤트 생성 및 outbox 저장
            PointCompletedEvent event = new PointCompletedEvent(orderId, userId, amount, cardAmount);
            saveOutBox(orderId, "PointCompleted", event);
        } catch (Exception e) {
            log.error("포인트 차감 실패 - 주문 ID : {}, 사유: {}", orderId, e.getMessage());

            // 실패 이벤트 생성 및 outbox 저장
            // 비니지니스 실패해도 이벤트를 발행해야 하므로 예외 번지지 않고 트랙잭션 커밋
            PointFailedEvent event = new PointFailedEvent(orderId, e.getMessage());
            saveOutBox(orderId, "PointFailed", event);
        }
    }

    private void saveOutBox(Long aggregateId, String eventType, Object payloadObj) {
        try {
            String payload = objectMapper.writeValueAsString(payloadObj);

            OutboxEvent event = OutboxEvent.builder()
                    .aggregateType("Point")
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payload)
                    .build();
            outboxRepository.save(event);
            log.info("Outbox 이벤트 저장 완료: {}, (ID: {})", event, aggregateId);
        } catch (JacksonException e) {
            log.error("이벤트 직렬화 실패", e);
            throw new RuntimeException("이벤트 처리 중 오류 발생");
        }
    }

    /**
     * 포인트 복구 로직 (보상 트랜잭션)
     */
    public void restore(Long userId, int amount) {
        pointRepository.findByUserId(userId).ifPresent(point -> {
            int currentBalance = point.getBalance();
            point.setBalance(currentBalance + amount);
            log.info("포인트 복구 완료 - 사용자: {}, 복구액: {}, 현재 잔액: {}", userId, amount, point.getBalance());
        });
    }
}