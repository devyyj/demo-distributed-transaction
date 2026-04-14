package com.joyopi.monolith.order.service;

import com.joyopi.monolith.order.domain.Order;
import com.joyopi.monolith.order.dto.OrderCreateCommand;
import com.joyopi.monolith.order.dto.OrderInfo;
import com.joyopi.monolith.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderInfo createOrder(OrderCreateCommand orderCreateCommand) {
        Order entity = orderCreateCommand.toEntity();
        Order saved = orderRepository.save(entity);
        return OrderInfo.from(saved);
    }
}
