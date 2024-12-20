package org.myexample.spinningmotion.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.interfc.StripeUseCase;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private StripeUseCase stripeUseCase;

    @InjectMocks
    private PaymentController paymentController;

    private CheckoutRequest checkoutRequest;
    private CheckoutResponse checkoutResponse;
    private String origin;
    private String payload;
    private String sigHeader;

    @BeforeEach
    void setUp() {
        checkoutRequest = new CheckoutRequest(); // Set necessary fields
        checkoutResponse = new CheckoutResponse(); // Set necessary fields
        origin = "http://localhost:3000";
        payload = "webhook-payload";
        sigHeader = "sig-header";
    }

    @Test
    void createCheckoutSession_ValidRequest_ReturnsSession() {
        // Arrange
        when(stripeUseCase.createCheckoutSession(checkoutRequest, origin))
                .thenReturn(checkoutResponse);

        // Act
        ResponseEntity<CheckoutResponse> response = paymentController.createCheckoutSession(checkoutRequest, origin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(checkoutResponse, response.getBody());
        verify(stripeUseCase).createCheckoutSession(checkoutRequest, origin);
    }

    @Test
    void handleStripeWebhook_ValidWebhook_ReturnsSuccess() {
        // Arrange
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Webhook processed");
        when(stripeUseCase.handleWebhook(payload, sigHeader))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> response = paymentController.handleStripeWebhook(payload, sigHeader);

        // Assert
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
        verify(stripeUseCase).handleWebhook(payload, sigHeader);
    }

    @Test
    void verifySession_ValidSession_ReturnsSuccess() {
        // Arrange
        String sessionId = "session_123";
        when(stripeUseCase.verifySession(sessionId)).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Boolean>> response = paymentController.verifySession(sessionId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("success"));
        verify(stripeUseCase).verifySession(sessionId);
    }

    @Test
    void verifySession_InvalidSession_ReturnsFalse() {
        // Arrange
        String sessionId = "invalid_session";
        when(stripeUseCase.verifySession(sessionId)).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Boolean>> response = paymentController.verifySession(sessionId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().get("success"));
        verify(stripeUseCase).verifySession(sessionId);
    }
}