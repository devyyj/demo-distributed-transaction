package com.joyopi.monolith.order.controller;

import com.joyopi.monolith.order.domain.Order;
import com.joyopi.monolith.order.dto.OrderRequest;
import com.joyopi.monolith.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
        Order order = orderService.createOrder(orderRequest);
        return ResponseEntity.ok("주문이 성공적으로 생성되었습니다. 주문 ID: " + order.getId());
    }
}
