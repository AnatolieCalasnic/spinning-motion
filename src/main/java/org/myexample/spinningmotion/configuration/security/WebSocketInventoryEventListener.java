package org.myexample.spinningmotion.configuration.security;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.domain.record.InventoryUpdate;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
public class WebSocketInventoryEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        String destination = event.getMessage().getHeaders().get("simpDestination", String.class);
        if ("/topic/inventory".equals(destination)) {
            // New client subscribed to inventory updates
            System.out.println("New client subscribed to inventory updates");
        }
    }

    // Add more event listeners as needed
    // For example, to listen for inventory updates directly:
    @EventListener
    public void handleInventoryUpdate(InventoryUpdate update) {
        messagingTemplate.convertAndSend("/topic/inventory", update);
    }
}