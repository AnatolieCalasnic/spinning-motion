package org.myexample.spinningmotion.controller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.domain.guest_user.GuestDetails;
import org.myexample.spinningmotion.persistence.GuestOrderRepository;
import org.myexample.spinningmotion.persistence.entity.GuestDetailsEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestOrderControllerTest {

    @Mock
    private GuestOrderRepository guestOrderRepository;

    @InjectMocks
    private GuestOrderController guestOrderController;

    private GuestDetails guestDetails;
    private GuestDetailsEntity guestDetailsEntity;

    @BeforeEach
    void setUp() {
        guestDetails = GuestDetails.builder()
                .purchaseHistoryId(1L)
                .fname("John")
                .lname("Doe")
                .email("john@example.com")
                .address("123 Main St")
                .postalCode("12345")
                .country("USA")
                .city("New York")
                .region("NY")
                .phonenum("123-456-7890")
                .build();

        guestDetailsEntity = GuestDetailsEntity.builder()
                .purchaseHistoryId(1L)
                .fname("John")
                .lname("Doe")
                .email("john@example.com")
                .address("123 Main St")
                .postalCode("12345")
                .country("USA")
                .city("New York")
                .region("NY")
                .phonenum("123-456-7890")
                .build();
    }

    @Test
    void getGuestOrder_ExistingOrder_ReturnsOrder() {
        // Arrange
        Long purchaseHistoryId = 1L;
        when(guestOrderRepository.findFirstByPurchaseHistoryId(purchaseHistoryId))
                .thenReturn(guestDetailsEntity);

        // Act
        ResponseEntity<GuestDetailsEntity> response = guestOrderController.getGuestOrder(purchaseHistoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(guestDetailsEntity, response.getBody());
        verify(guestOrderRepository).findFirstByPurchaseHistoryId(purchaseHistoryId);
    }

    @Test
    void getGuestOrder_NonExistingOrder_ReturnsNotFound() {
        // Arrange
        Long purchaseHistoryId = 1L;
        when(guestOrderRepository.findFirstByPurchaseHistoryId(purchaseHistoryId))
                .thenReturn(null);

        // Act
        ResponseEntity<GuestDetailsEntity> response = guestOrderController.getGuestOrder(purchaseHistoryId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(guestOrderRepository).findFirstByPurchaseHistoryId(purchaseHistoryId);
    }

    @Test
    void createGuestOrder_ValidDetails_ReturnsCreatedOrder() {
        // Arrange
        when(guestOrderRepository.save(any(GuestDetailsEntity.class)))
                .thenReturn(guestDetailsEntity);

        // Act
        ResponseEntity<GuestDetailsEntity> response = guestOrderController.createGuestOrder(guestDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(guestDetailsEntity, response.getBody());
        verify(guestOrderRepository).save(any(GuestDetailsEntity.class));
    }

    @Test
    void createGuestOrder_SaveError_ReturnsInternalServerError() {
        // Arrange
        when(guestOrderRepository.save(any(GuestDetailsEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<GuestDetailsEntity> response = guestOrderController.createGuestOrder(guestDetails);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(guestOrderRepository).save(any(GuestDetailsEntity.class));
    }

    @Test
    void getGuestOrdersByOrder_ExistingOrders_ReturnsOrders() {
        // Arrange
        Long orderId = 1L;
        List<GuestDetailsEntity> expectedOrders = Arrays.asList(guestDetailsEntity);
        when(guestOrderRepository.findAllByPurchaseHistoryId(orderId))
                .thenReturn(expectedOrders);

        // Act
        ResponseEntity<List<GuestDetailsEntity>> response = guestOrderController.getGuestOrdersByOrder(orderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedOrders, response.getBody());
        verify(guestOrderRepository).findAllByPurchaseHistoryId(orderId);
    }

    @Test
    void getGuestOrdersByOrder_NoOrders_ReturnsNotFound() {
        // Arrange
        Long orderId = 1L;
        when(guestOrderRepository.findAllByPurchaseHistoryId(orderId))
                .thenReturn(List.of());

        // Act
        ResponseEntity<List<GuestDetailsEntity>> response = guestOrderController.getGuestOrdersByOrder(orderId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(guestOrderRepository).findAllByPurchaseHistoryId(orderId);
    }
}
