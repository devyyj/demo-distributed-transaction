package com.joyopi.monolith.point.service;

import com.joyopi.monolith.point.domain.Point;
import com.joyopi.monolith.point.dto.PointUsageResponse;
import com.joyopi.monolith.point.dto.PointUseCommand;
import com.joyopi.monolith.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포인트 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private static final Long DEFAULT_POINT = 10000L;

    /**
     * 포인트를 사용합니다. 사용자가 없으면 기본 포인트를 지급 후 차감합니다.
     */
    @Transactional
    public PointUsageResponse usePoint(PointUseCommand command) {
        // [의도적 실패 코드] 잔액 부족 예외를 강제로 발생시키려면 아래 주석을 해제하세요.
        // if (command.getAmount() > 0) throw new com.joyopi.monolith.common.exception.BusinessException("의도적인 포인트 사용 실패");

        Point point = pointRepository.findByUserId(command.getUserId())
                .orElseGet(() -> pointRepository.save(Point.builder()
                        .userId(command.getUserId())
                        .balance(DEFAULT_POINT)
                        .build()));

        point.use(command.getAmount());

        return PointUsageResponse.builder()
                .userId(point.getUserId())
                .remainingBalance(point.getBalance())
                .build();
    }
}
