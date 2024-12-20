package org.myexample.spinningmotion.business.impl.notification;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.domain.notification.NotificationMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationUseCaseImpl {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendAuthenticationNotification(String message, String type) {
        NotificationMessage notification = new NotificationMessage(message, type);
        messagingTemplate.convertAndSend("/topic/auth", notification);
    }

    public void sendLoginNotification(String email) {
        NotificationMessage notification = new NotificationMessage(
                "User logged in: " + email,
                "SUCCESS"
        );
        messagingTemplate.convertAndSend("/topic/auth", notification);
    }
}