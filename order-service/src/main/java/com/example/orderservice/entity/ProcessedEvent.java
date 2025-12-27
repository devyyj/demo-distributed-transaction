package com.example.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 중복 처리 방지를 위한 처리 완료 이벤트 기록 엔티티
 */
@Entity
@Getter
@Table(name = "processed_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    private String eventId;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    public static ProcessedEvent of(String key) {
        return new ProcessedEvent(key, LocalDateTime.now());
    }
}