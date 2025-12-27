package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.entity.OutboxEvent;
import com.example.orderservice.entity.ProcessedEvent;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.OutboxRepository;
import com.example.orderservice.repository.ProcessedEventRepository;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public record OrderCreatedEvent(Long orderId, Long userId, int pointAmount, int cardAmount) {
    }

    /**
     * 주문 생성 및 Saga 시작 (클라이언트 requestId 기반 멱등성 처리)
     */
    public void processOrder(String requestId, Long userId, int totalAmount, int pointAmount) {
        // 1. requestId를 이용한 멱등성 키 생성
        String idempotencyKey = "order-request-" + requestId;

        // 2. 이미 처리된 요청인지 확인
        if (processedEventRepository.existsById(idempotencyKey)) {
            log.info("중복된 주문 요청입니다. (Request ID: {})", requestId);
            return;
        }

        // 3. 비즈니스 로직: 주문 엔티티 생성 및 저장
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .pointAmount(pointAmount)
                .build();
        orderRepository.save(order);

        log.info("Saga 시작 - 주문 생성(PENDING): {}, RequestID: {}", order.getId(), requestId);

        // 4. 멱등성 테이블(ProcessedEvent)에 기록 저장
        processedEventRepository.save(ProcessedEvent.of(idempotencyKey));

        // 5. Outbox 이벤트 저장
        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), userId, pointAmount, order.getCardAmount());

        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("order")
                    .aggregateId(order.getId())
                    .eventType("OrderCreated")
                    .payload(payload)
                    .build();

            outboxRepository.save(outboxEvent);
            log.info("Outbox 이벤트 저장 완료: {}", outboxEvent.getId());
        } catch (JacksonException e) {
            log.error("이벤트 직렬화 실패", e);
            throw new RuntimeException("주문 생성 처리 중 오류 발생");
        }
    }

    /**
     * 주문 상태 변경 (내부 이벤트 수신 시 멱등성 처리)
     */
    public void updateStatus(Long orderId, OrderStatus status) {
        String idempotencyKey = String.format("order-update-%s-%d", status.name(), orderId);

        if (processedEventRepository.existsById(idempotencyKey)) {
            log.info("이미 처리된 상태 변경 요청입니다. (주문 ID: {}, 상태: {})", orderId, status.name());
            return;
        }

        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
            processedEventRepository.save(ProcessedEvent.of(idempotencyKey));
            log.info("주문 상태 변경 완료: {} (ID: {})", status, orderId);
        });
    }
}