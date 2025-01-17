package org.myexample.spinningmotion.business.impl.record;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.InventoryTrackingUseCase;
import org.myexample.spinningmotion.domain.record.InventoryUpdate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryTrackingUseCaseImpl implements InventoryTrackingUseCase {
    private final SimpMessagingTemplate messagingTemplate;
    private static final String INVENTORY_TOPIC = "/topic/inventory";

    public void broadcastInventoryUpdate(InventoryUpdate update) {
        messagingTemplate.convertAndSend(INVENTORY_TOPIC, update);
    }
}