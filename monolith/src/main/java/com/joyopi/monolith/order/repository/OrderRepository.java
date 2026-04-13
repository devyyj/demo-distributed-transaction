package com.joyopi.monolith.order.repository;

import com.joyopi.monolith.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
