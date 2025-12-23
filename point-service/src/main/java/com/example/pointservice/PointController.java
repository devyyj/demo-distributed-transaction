package com.example.pointservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/deduct")
    public ResponseEntity<String> deduct(@RequestBody PointRequest pointRequest) {
        pointService.deductPoint(pointRequest.userId(), pointRequest.amount());
        return ResponseEntity.ok("포인트 차감 성공. " + pointRequest.toString());
    }

    @PostMapping("/restore")
    public ResponseEntity<String> restore(@RequestBody PointRequest pointRequest) {
        pointService.restorePoint(pointRequest.userId(), pointRequest.amount());
        return ResponseEntity.ok("포인트 차감 성공. " + pointRequest.toString());
    }
}
