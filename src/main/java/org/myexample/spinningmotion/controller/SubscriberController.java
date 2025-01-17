package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.SubscriberUseCase;
import org.myexample.spinningmotion.domain.subscriber.SubscribeRequest;
import org.myexample.spinningmotion.domain.subscriber.SubscribeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriber")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SubscriberController {
    private final SubscriberUseCase subscriberUseCase;

    @PostMapping
    public ResponseEntity<SubscribeResponse> subscribe(@RequestBody SubscribeRequest request) {
        SubscribeResponse response = subscriberUseCase.subscribe(request);
        return ResponseEntity.ok(response);
    }
}