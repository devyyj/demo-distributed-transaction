package com.example.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "orders")
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private int cardAmount;
    private int pointAmount;
    private int totalAmount;

    @Setter
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Builder
    public Order(Long userId, int pointAmount, int totalAmount) {
        if (userId == null) throw new IllegalArgumentException("사용자 ID는 필수입니다.");

        if (pointAmount < 0 || totalAmount < 0) throw new IllegalArgumentException("결제 금액은 음수일 수 없습니다.");

        if (totalAmount < pointAmount) throw new IllegalArgumentException("포인트 사용액이 총 결제 금액보다 클 수 없습니다.");

        this.userId = userId;
        this.pointAmount = pointAmount;
        this.totalAmount = totalAmount;
        this.cardAmount = totalAmount - pointAmount;
        this.status = OrderStatus.PENDING;
    }
}
