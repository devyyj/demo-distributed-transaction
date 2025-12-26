package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.entity.OutboxEvent;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.OutboxRepository;
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
    private final ObjectMapper objectMapper; // JSON 변환을 위한 객체

    // DTO 정의 (내부 record)
    public record OrderCreatedEvent(Long orderId, Long userId, int pointAmount, int cardAmount) {
    }

    /**
     * 주문 생성 및 Saga 시작
     * 발행 토픽: order.created
     */
    public void processOrder(Long userId, int totalAmount, int pointAmount) {
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .pointAmount(pointAmount)
                .build();
        orderRepository.save(order);

        log.info("Saga 시작 - 주문 생성(PENDING): {}", order.getId());

        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), userId, pointAmount, order.getCardAmount());

        // 카프카에 직접 메시지를 보내지 않음
        // streamBridge.send("orderCreated-out-0", event);

        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("Order")
                    .aggregateId(order.getId())
                    .eventType("OrderCreated")
                    .payload(payload)
                    .build();

            outboxRepository.save(outboxEvent);
            log.info("Outbox 이벤트 저장 완료: {}", outboxEvent.getId());
        } catch (JacksonException e) {
            log.error("이벤트 직렬화 실패", e);
            throw new RuntimeException("주문 생성 처리 중 오류 밟생");
        }

    }

    public void updateStatus(Long orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
            log.info("주문 상태 변경 완료: {}", status);
        });
    }
}