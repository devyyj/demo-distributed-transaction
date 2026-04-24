package com.joyopi.monolith.point.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 포인트 엔티티입니다.
 */
@Entity
@Table(name = "points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Long balance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Point(Long userId, Long balance) {
        this.userId = userId;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 포인트를 차감합니다.
     */
    public void use(Long amount) {
        if (this.balance < amount) {
            throw new com.joyopi.monolith.common.exception.BusinessException("포인트 잔액이 부족합니다.");
        }
        this.balance -= amount;
    }
}
