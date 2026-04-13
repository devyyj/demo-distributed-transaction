package com.joyopi.monolith.order.service;

import com.joyopi.monolith.order.domain.Order;
import com.joyopi.monolith.order.dto.OrderRequest;
import com.joyopi.monolith.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public Order createOrder(OrderRequest orderRequest) {
        Order build = Order.builder()
                .userId(orderRequest.userId())
                .productId(orderRequest.productId())
                .totalAmount(orderRequest.totalAmount())
                .pointAmount(orderRequest.pointAmount())
                .build();
        return orderRepository.save(build);
    }
}
