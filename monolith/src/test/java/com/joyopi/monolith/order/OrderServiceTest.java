package com.joyopi.monolith.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.joyopi.monolith.order.domain.OrderStatus;
import com.joyopi.monolith.order.dto.OrderCreateCommand;
import com.joyopi.monolith.order.dto.OrderResponse;
import com.joyopi.monolith.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 서비스 통합 테스트
 */
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("사용자가 상품을 주문하면 포인트가 차감되고 결제가 완료되어 주문이 생성된다.")
    void createOrderSuccess() {
        // given
        OrderCreateCommand command = OrderCreateCommand.builder()
                .userId(1L)
                .productId(1001L)
                .totalAmount(10000L)
                .pointAmount(2000L)
                .build();

        // when
        OrderResponse response = orderService.createOrder(command);

        // then
        assertThat(response.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(response.getOrderId()).isNotNull();
    }
}
