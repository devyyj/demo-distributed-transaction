package com.example.pointservice.service;

import com.example.pointservice.entity.OutboxEvent;
import com.example.pointservice.entity.Point;
import com.example.pointservice.entity.ProcessedEvent;
import com.example.pointservice.repository.OutboxRepository;
import com.example.pointservice.repository.PointRepository;
import com.example.pointservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final PointRepository pointRepository;
    private final OutboxRepository outboxRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public record PointCompletedEvent(Long orderId, Long userId, int pointAmount, int cardAmount) {
    }

    public record PointFailedEvent(Long orderId, String reason) {
    }

    /**
     * 포인트 차감 로직 (멱등성 처리 적용)
     */
    public void deduct(Long orderId, Long userId, int amount, int cardAmount) {
        String idempotencyKey = "point-deduct-" + orderId;

        if (processedEventRepository.existsById(idempotencyKey)) {
            log.info("이미 처리된 포인트 차감 요청입니다. (주문 ID: {})", orderId);
            return;
        }

        try {
            Point point = pointRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            point.use(amount);
            log.info("포인트 차감 완료 - 사용자: {}, 잔액: {}", userId, point.getBalance());

            processedEventRepository.save(ProcessedEvent.of(idempotencyKey));

            PointCompletedEvent event = new PointCompletedEvent(orderId, userId, amount, cardAmount);
            saveOutBox(orderId, "PointCompleted", event);
        } catch (Exception e) {
            log.error("포인트 차감 실패 - 주문 ID : {}, 사유: {}", orderId, e.getMessage());
            PointFailedEvent event = new PointFailedEvent(orderId, e.getMessage());
            saveOutBox(orderId, "PointFailed", event);
        }
    }

    private void saveOutBox(Long aggregateId, String eventType, Object payloadObj) {
        try {
            String payload = objectMapper.writeValueAsString(payloadObj);

            OutboxEvent event = OutboxEvent.builder()
                    .aggregateType("point")
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payload)
                    .build();
            outboxRepository.save(event);
            log.info("Outbox 이벤트 저장 완료: {}, (ID: {})", eventType, aggregateId);
        } catch (JacksonException e) {
            log.error("이벤트 직렬화 실패", e);
            throw new RuntimeException("이벤트 처리 중 오류 발생");
        }
    }

    /**
     * 포인트 복구 로직 (보상 트랜잭션 멱등성 적용)
     */
    public void restore(Long userId, int amount, Long orderId) {
        String idempotencyKey = "point-restore-" + orderId;

        if (processedEventRepository.existsById(idempotencyKey)) {
            log.info("이미 복구된 포인트 요청입니다. (주문 ID: {})", orderId);
            return;
        }

        pointRepository.findByUserId(userId).ifPresent(point -> {
            point.setBalance(point.getBalance() + amount);
            processedEventRepository.save(ProcessedEvent.of(idempotencyKey));
            log.info("포인트 복구 완료 - 사용자: {}, 복구액: {}", userId, amount);
        });
    }
}