package com.example.orderservice.service;

import com.example.orderservice.dto.CardApproveRequest;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.dto.PointDeductRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final OrderRepository orderRepository;

    public void processOrder(Long userId, int totalAmount, int pointAmount) {

        // 주문 생성
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .pointAmount(pointAmount)
                .build();

        log.info("결제 주문 생성 완료 : {}", order);

        // 포인트 차감
        // 요청 바디 생성
        PointDeductRequest pointDeductRequest = new PointDeductRequest(userId, pointAmount);
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 바디와 헤더 합치기
        HttpEntity<PointDeductRequest> request = new HttpEntity<>(pointDeductRequest, headers);
        // 요청 전송
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8081/api/points/deduct", request, String.class);
        log.info("포인트 차감 응답 : {}", response.getBody());

        // 외부 카드사 승인 요청
        // 요청 바디 생성
        CardApproveRequest cardApproveRequest = new CardApproveRequest(order.getCardAmount());
        // 바디와 헤더 합치기
        HttpEntity<CardApproveRequest> approveRequest = new HttpEntity<>(cardApproveRequest, headers);
        // 요청 전송
        try {
            ResponseEntity<String> approveResponse = restTemplate.postForEntity("http://localhost:8082/api/cards/approve", approveRequest, String.class);
            log.info("카드 결제 응답 : {}", approveResponse.getBody());
        } catch (Exception e) {
            // 보상 트랜잭션
            log.error("카드 결제 실패로 인한 보상 트랜잭션");
            ResponseEntity<String> restorePointResponse = restTemplate.postForEntity("http://localhost:8081/api/points/restore", request, String.class);
            log.info("포인트 차감 응답 : {}", restorePointResponse.getBody());

            throw new RuntimeException("카드 결제 실패로 결제가 취소되고 포인트가 복구되었습니다.");
        }

        // 모든 단계 성공 시 완료 상태로 업데이트
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        log.info("결제 완료");
    }
}
