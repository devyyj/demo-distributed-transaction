package com.joyopi.monolith.order.dto;

import com.joyopi.monolith.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponse {
    private Long orderId;
    private OrderStatus status;
}
