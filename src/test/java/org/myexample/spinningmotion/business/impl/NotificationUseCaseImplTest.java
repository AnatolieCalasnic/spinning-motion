package org.myexample.spinningmotion.business.impl;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.impl.notification.NotificationUseCaseImpl;
import org.myexample.spinningmotion.domain.notification.NotificationMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationUseCaseImplTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationUseCaseImpl notificationUseCase;

    @Test
    void sendAuthenticationNotification_Success() {
        // Given
        String message = "Test message";
        String type = "SUCCESS";

        // When
        notificationUseCase.sendAuthenticationNotification(message, type);

        // Then
        verify(messagingTemplate).convertAndSend(
                eq("/topic/auth"),
                eq(new NotificationMessage(message, type))
        );
    }

    @Test
    void sendLoginNotification_Success() {
        // Given
        String email = "test@example.com";

        // When
        notificationUseCase.sendLoginNotification(email);

        // Then
        verify(messagingTemplate).convertAndSend(
                eq("/topic/auth"),
                eq(new NotificationMessage("User logged in: " + email, "SUCCESS"))
        );
    }
}