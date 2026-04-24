package com.joyopi.monolith.order.controller;

import com.joyopi.monolith.common.dto.ApiResponse;
import com.joyopi.monolith.order.dto.OrderCreateCommand;
import com.joyopi.monolith.order.dto.OrderCreateRequest;
import com.joyopi.monolith.order.dto.OrderResponse;
import com.joyopi.monolith.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 주문 API 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderCreateRequest request) {
        // 컨트롤러에서 서비스용 Command로 변환
        OrderCreateCommand command = OrderCreateCommand.builder()
                .userId(request.getUserId())
                .productId(request.getProductId())
                .totalAmount(request.getTotalAmount())
                .pointAmount(request.getPointAmount())
                .build();

        OrderResponse response = orderService.createOrder(command);
        return ApiResponse.success(response);
    }
}
