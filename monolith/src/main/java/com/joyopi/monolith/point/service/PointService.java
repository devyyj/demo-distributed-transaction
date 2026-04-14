package com.joyopi.monolith.point.service;

import com.joyopi.monolith.point.domain.Point;
import com.joyopi.monolith.point.dto.PointInfo;
import com.joyopi.monolith.point.dto.PointUpdateCommand;
import com.joyopi.monolith.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {
    private final PointRepository pointRepository;

    public PointInfo usePoint(PointUpdateCommand command){
        Point point = pointRepository.findByUserId(command.userId());
        point.use(command.pointAmount());
        Point updated = pointRepository.save(point);
        return PointInfo.from(updated);
    }
}
