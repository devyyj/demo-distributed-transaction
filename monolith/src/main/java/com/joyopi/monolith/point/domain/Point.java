package com.joyopi.monolith.point.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "points")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private int balance;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Point(Long userId, int balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public void use(int amount) {
        if (this.balance < amount) {
            throw new IllegalStateException("포인트 잔액이 부족합니다. 잔액: " + this.balance + ", 요청: " + amount);
        }
        this.balance -= amount;
    }
}
