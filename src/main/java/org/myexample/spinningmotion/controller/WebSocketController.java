package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.impl.user.UserTrackingUseCaseImpl;
import org.myexample.spinningmotion.business.interfc.RecordUseCase;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final UserTrackingUseCaseImpl userTrackingUseCase;
    private final RecordUseCase recordUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/request-active-users")
    public void handleActiveUsersRequest() {
        broadcastActiveUserCount();
    }
    @MessageMapping("/request-inventory")
    public void handleInventoryRequest() {
        broadcastInventory();
    }
    private void broadcastActiveUserCount() {
        messagingTemplate.convertAndSend("/topic/active-users",
                userTrackingUseCase.getActiveUsersCount());
    }
    private void broadcastInventory() {
        messagingTemplate.convertAndSend("/topic/inventory",
                recordUseCase.getAllRecords());
    }
}