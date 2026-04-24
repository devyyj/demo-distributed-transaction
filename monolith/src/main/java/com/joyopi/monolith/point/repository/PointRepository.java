package com.joyopi.monolith.point.repository;

import com.joyopi.monolith.point.domain.Point;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByUserId(Long userId);
}
