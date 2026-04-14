package com.joyopi.monolith.order.controller;

import com.joyopi.monolith.order.dto.OrderCreateRequest;
import com.joyopi.monolith.order.dto.OrderInfo;
import com.joyopi.monolith.order.dto.OrderResponse;
import com.joyopi.monolith.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderCreateRequest orderCreateRequest) {
        OrderInfo order = orderService.createOrder(orderCreateRequest.toCommand());
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}
