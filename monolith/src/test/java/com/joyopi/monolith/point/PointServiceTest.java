package com.joyopi.monolith.point;

import static org.assertj.core.api.Assertions.assertThat;

import com.joyopi.monolith.common.exception.BusinessException;
import com.joyopi.monolith.point.dto.PointUsageResponse;
import com.joyopi.monolith.point.dto.PointUseCommand;
import com.joyopi.monolith.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포인트 서비스 기능 테스트
 */
@SpringBootTest
@Transactional
class PointServiceTest {

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("새로운 사용자가 포인트를 사용하려고 하면 기본 포인트 10000에서 차감된다.")
    void usePointForNewUser() {
        // given
        PointUseCommand command = PointUseCommand.builder()
                .userId(1L)
                .amount(3000L)
                .build();

        // when
        PointUsageResponse response = pointService.usePoint(command);

        // then
        assertThat(response.getRemainingBalance()).isEqualTo(7000L);
    }

    @Test
    @DisplayName("잔액보다 많은 포인트를 사용하려고 하면 예외가 발생한다.")
    void usePointExceedingBalance() {
        // given
        PointUseCommand command = PointUseCommand.builder()
                .userId(2L)
                .amount(20000L) // 기본 10000보다 큼
                .build();

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(BusinessException.class, () -> {
            pointService.usePoint(command);
        });
    }
}
