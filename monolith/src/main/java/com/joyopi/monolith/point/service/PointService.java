package com.joyopi.monolith.point.service;

import com.joyopi.monolith.point.domain.Point;
import com.joyopi.monolith.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    @Transactional
    public void usePoints(Long userId, int amount) {
        if (amount == 0) {
            return;
        }
        // 사용자 포인트 조회, 없으면 잔액 0으로 생성
        Point point = pointRepository.findByUserId(userId)
                .orElseGet(() -> pointRepository.save(Point.builder()
                        .userId(userId)
                        .balance(0)
                        .build()));
        point.use(amount);
    }
}
