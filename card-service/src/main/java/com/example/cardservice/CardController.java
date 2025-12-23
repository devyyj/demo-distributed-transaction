package com.example.cardservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/approve")
    public ResponseEntity<String> approve(@RequestBody CardRequest cardRequest) {
        cardService.approve(cardRequest.amount());
        return ResponseEntity.ok("카드 결제 완료 : " + cardRequest.amount());
    }

}
