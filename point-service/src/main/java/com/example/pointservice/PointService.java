package com.example.pointservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포인트 비즈니스 로직 서비스
 * - 데이터베이스 트랜잭션을 관리하며 엔티티의 상태 변경을 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final PointRepository pointRepository;

    /**
     * 포인트 차감 로직
     */
    public void deduct(Long userId, int amount) {
        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (point.getBalance() < amount) {
            throw new IllegalStateException("잔액이 부족합니다. 현재 잔액: " + point.getBalance());
        }

        // 엔티티 상태 변경 (Dirty Checking에 의해 save() 호출 없이 DB 반영)
        point.use(amount);
        log.info("포인트 차감 완료 - 사용자: {}, 차감액: {}, 현재 잔액: {}", userId, amount, point.getBalance());
    }

    /**
     * 포인트 복구 로직 (보상 트랜잭션)
     */
    public void restore(Long userId, int amount) {
        pointRepository.findByUserId(userId).ifPresent(point -> {
            int currentBalance = point.getBalance();
            point.setBalance(currentBalance + amount);
            log.info("포인트 복구 완료 - 사용자: {}, 복구액: {}, 현재 잔액: {}", userId, amount, point.getBalance());
        });
    }
}