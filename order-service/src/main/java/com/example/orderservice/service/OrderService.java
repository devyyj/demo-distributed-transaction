package com.example.orderservice.service;

import com.example.orderservice.common.OrderStatus;
import com.example.orderservice.dto.CardApproveRequest;
import com.example.orderservice.dto.CardResultEvent;
import com.example.orderservice.dto.OrderEvent;
import com.example.orderservice.dto.PointEvent;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final StreamBridge streamBridge;
    private final RestTemplate restTemplate = new RestTemplate();
    private final OrderRepository orderRepository;

    @Transactional
    public void processOrder(Long userId, int totalAmount, int pointAmount) {
        // 주문 생성
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .pointAmount(pointAmount)
                .build();
        orderRepository.save(order);
        log.info("결제 주문 생성 완료 (PENDING) : {}", order);

        // 포인트 이벤트 발행
        streamBridge.send("orderCreated-out-0", new OrderEvent(order.getId(), userId, pointAmount));

    }

    @Bean
    public Consumer<PointEvent> pointDeductedConsumer() {
        return event -> {
            log.info("포인트 차감 확인 : 주문 번호 {}", event.orderId());

            try {
                CardApproveRequest request = new CardApproveRequest(event.amount());
                restTemplate.postForEntity("http://localhost:8082/api/cards/approve", request, String.class);

                streamBridge.send("cardResult-out-0", new CardResultEvent(event.orderId(), event.userId(), event.amount(), "SUCCESS"));

            } catch (Exception e) {
                log.error("카드 결제 실패: {}", e.getMessage());

                streamBridge.send("cardResult-out-0", new CardResultEvent(event.orderId(), event.userId(), event.amount(), "FAILED"));
            }
        };
    }

    @Bean
    public Consumer<CardResultEvent> cardResultConsumer() {
        return event -> {
            orderRepository.findById(event.orderId()).ifPresent(order -> {
                if ("SUCCESS".equals(event.status())) {
                    order.setStatus(OrderStatus.COMPLETED);
                    log.info("결제 최종 완료! : {}", event.orderId());
                } else {
                    order.setStatus(OrderStatus.FAILED);
                    log.error("결제 실패! : {}", event.orderId());
                }
                orderRepository.save(order);
            });
        };
    }
}
