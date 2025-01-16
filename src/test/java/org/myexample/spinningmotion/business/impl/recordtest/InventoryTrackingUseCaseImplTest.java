package org.myexample.spinningmotion.business.impl.recordtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.impl.record.InventoryTrackingUseCaseImpl;
import org.myexample.spinningmotion.domain.record.InventoryUpdate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class InventoryTrackingUseCaseImplTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private InventoryTrackingUseCaseImpl inventoryTrackingUseCase;

    @BeforeEach
    void setUp() {
        inventoryTrackingUseCase = new InventoryTrackingUseCaseImpl(messagingTemplate);
    }

    @Test
    void broadcastInventoryUpdate_ShouldSendMessageToTopic() {
        // Arrange
        InventoryUpdate update = InventoryUpdate.builder()
                .recordId(1L)
                .title("Test Record")
                .quantity(10)
                .updateType("STOCK_UPDATE")
                .build();
        String expectedTopic = "/topic/inventory";

        // Act
        inventoryTrackingUseCase.broadcastInventoryUpdate(update);

        // Assert
        verify(messagingTemplate, times(1))
                .convertAndSend(expectedTopic, update);
    }

    @Test
    void broadcastInventoryUpdate_WithDifferentUpdateTypes_ShouldSendMessages() {
        // Arrange
        InventoryUpdate stockUpdate = InventoryUpdate.builder()
                .recordId(1L)
                .title("Vinyl Record")
                .quantity(5)
                .updateType("STOCK_DECREASE")
                .build();

        InventoryUpdate restockUpdate = InventoryUpdate.builder()
                .recordId(1L)
                .title("Vinyl Record")
                .quantity(15)
                .updateType("RESTOCK")
                .build();

        String expectedTopic = "/topic/inventory";

        // Act
        inventoryTrackingUseCase.broadcastInventoryUpdate(stockUpdate);
        inventoryTrackingUseCase.broadcastInventoryUpdate(restockUpdate);

        // Assert
        verify(messagingTemplate, times(1))
                .convertAndSend(expectedTopic, stockUpdate);
        verify(messagingTemplate, times(1))
                .convertAndSend(expectedTopic, restockUpdate);
    }

    @Test
    void broadcastInventoryUpdate_WithZeroQuantity_ShouldSendMessage() {
        // Arrange
        InventoryUpdate update = InventoryUpdate.builder()
                .recordId(1L)
                .title("Out of Stock Record")
                .quantity(0)
                .updateType("OUT_OF_STOCK")
                .build();
        String expectedTopic = "/topic/inventory";

        // Act
        inventoryTrackingUseCase.broadcastInventoryUpdate(update);

        // Assert
        verify(messagingTemplate, times(1))
                .convertAndSend(expectedTopic, update);
    }
}