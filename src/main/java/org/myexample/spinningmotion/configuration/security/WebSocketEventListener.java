package org.myexample.spinningmotion.configuration.security;


import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.impl.user.UserTrackingUseCaseImpl;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final UserTrackingUseCaseImpl userTrackingUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        userTrackingUseCase.addUser(sessionId, "anonymous");
        broadcastActiveUserCount();
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        userTrackingUseCase.removeUser(sessionId);
        broadcastActiveUserCount();
    }

    private void broadcastActiveUserCount() {
        messagingTemplate.convertAndSend("/topic/active-users",
                userTrackingUseCase.getActiveUsersCount());
    }
}