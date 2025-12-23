package com.example.pointservice;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "points")
public class Point {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Setter
    private int balance;

    public void use(int amount) {
        if (this.balance < amount) {
            throw new RuntimeException("포인트 부족. 잔액 : " + this.balance);
        }
        this.balance -= amount;
    }
}
