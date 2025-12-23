package com.example.pointservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PointService {
    private final PointRepository pointRepository;

    public void deductPoint(Long userId, int amount) {
        Point point = pointRepository.findByUserId(userId).orElseThrow();
        point.use(amount);
        log.info("포인트 차감 : {} 포인트", amount);
    }

    public void restorePoint(Long userId, int amount) {
        Point point = pointRepository.findByUserId(userId).orElseThrow();
        int balance = point.getBalance();
        point.setBalance(balance + amount);
        log.info("기존 포인트 : {}. 충전 포인트 : {}. 현재 포인트 {}", balance, amount, point.getBalance());
    }
}
