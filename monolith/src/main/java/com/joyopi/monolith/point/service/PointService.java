package com.joyopi.monolith.point.service;

import com.joyopi.monolith.point.domain.Point;
import com.joyopi.monolith.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    @Value("${point.default-balance}")
    private int defaultBalance;

    @Transactional
    public void usePoints(Long userId, int amount) {
        if (amount == 0) {
            log.debug("포인트 사용 금액이 0이므로 처리를 건너뜁니다. userId: {}", userId);
            return;
        }

        // 사용자 포인트 조회, 없으면 기본 포인트로 생성
        Point point = pointRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("사용자 포인트 정보 없음, 기본 포인트({})로 생성 - userId: {}", defaultBalance, userId);
                    return pointRepository.save(Point.builder()
                            .userId(userId)
                            .balance(defaultBalance)
                            .build());
                });

        log.info("포인트 차감 시작 - userId: {}, 현재 잔액: {}, 차감 금액: {}", userId, point.getBalance(), amount);

        // [실패 케이스] 포인트 잔액 부족 시뮬레이션
        // throw new InsufficientPointException(point.getBalance(), amount);

        point.use(amount);
        log.info("포인트 차감 완료 - userId: {}, 차감 후 잔액: {}", userId, point.getBalance());
    }
}
