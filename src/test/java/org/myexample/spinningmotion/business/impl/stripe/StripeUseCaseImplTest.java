package org.myexample.spinningmotion.business.impl.stripe;

import com.stripe.Stripe;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.interfc.PurchaseHistoryUseCase;
import org.myexample.spinningmotion.business.interfc.RecordUseCase;
import org.myexample.spinningmotion.domain.purchase_history.CreatePurchaseHistoryResponse;
import org.myexample.spinningmotion.domain.record.GetRecordResponse;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutResponse;
import org.myexample.spinningmotion.persistence.GuestOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeUseCaseImplTest {


    @Mock
    private PurchaseHistoryUseCase purchaseHistoryUseCase;
    @Mock
    private RecordUseCase recordUseCase;
    @Mock
    private GuestOrderRepository guestOrderRepository;
    @InjectMocks
    private StripeUseCaseImpl stripeUseCase;

    private CheckoutRequest checkoutRequest;
    private static final String TEST_ORIGIN = "http://localhost:3000";
    private static final String TEST_SESSION_ID = "test_session_123";
    private static final String TEST_CLIENT_SECRET = "test_client_secret_456";
    private static final String TEST_SECRET_KEY = "sk_test_123";
    private static final String TEST_WEBHOOK_SECRET = "whsec_123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripeUseCase, "stripeSecretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(stripeUseCase, "webhookSecret", TEST_WEBHOOK_SECRET);

        CheckoutRequest.Item testItem = new CheckoutRequest.Item();
        testItem.setTitle("Test Album");
        testItem.setArtist("Test Artist");
        testItem.setPrice(19.99);
        testItem.setQuantity(2);
        testItem.setCondition("New");

        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setItems(Collections.singletonList(testItem));
        Map<String, String> metadata = new HashMap<>();
        metadata.put("isGuest", "true");
        checkoutRequest.setMetadata(metadata);

    }

    @Test
    void createCheckoutSession_Success()  {
        // Given
        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);
        when(mockSession.getClientSecret()).thenReturn(TEST_CLIENT_SECRET);

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            // When
            CheckoutResponse response = stripeUseCase.createCheckoutSession(checkoutRequest, TEST_ORIGIN);

            // Then
            assertNotNull(response);
            assertEquals(TEST_SESSION_ID, response.getSessionId());
            assertEquals(TEST_CLIENT_SECRET, response.getClientSecret());

            // Verify Stripe API key was set
            assertEquals(TEST_SECRET_KEY, Stripe.apiKey);
        }
    }

    @Test
    void createCheckoutSession_StripeException() {
        // Given
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(new InvalidRequestException(
                            "Test error",         // message
                            "param",              // param
                            "req_123",            // requestId
                            "code",               // code
                            400,                  // statusCode
                            null                  // cause
                    ));

            // When/Then
            assertThrows(RuntimeException.class,
                    () -> stripeUseCase.createCheckoutSession(checkoutRequest, TEST_ORIGIN));
        }
    }

    @Test
    void handleWebhook_Success()  {
        // Given
        String testItems = "[{\"recordId\":1,\"title\":\"Test Record\",\"artist\":\"Test Artist\",\"quantity\":1,\"price\":29.99,\"condition\":\"New\"}]";
        String payload = String.format("""
        {
            "type": "checkout.session.completed",
            "data": {
                "object": {
                    "metadata": {
                        "items": "%s",
                        "isGuest": "true"
                    }
                }
            }
        }""", testItems.replace("\"", "\\\""));

        String sigHeader = "test_sig";

        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("checkout.session.completed");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            CreatePurchaseHistoryResponse purchaseResponse = CreatePurchaseHistoryResponse.builder()
                    .id(1L)
                    .build();
            when(purchaseHistoryUseCase.createPurchaseHistory(any()))
                    .thenReturn(purchaseResponse);

            when(recordUseCase.getRecord(any()))
                    .thenReturn(GetRecordResponse.builder()
                            .id(1L)
                            .title("Test Record")
                            .artist("Test Artist")
                            .quantity(5)
                            .price(29.99)
                            .condition("New")
                            .build());

            // When
            ResponseEntity<String> response = stripeUseCase.handleWebhook(payload, sigHeader);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Checkout session processed successfully", response.getBody());
            verify(purchaseHistoryUseCase).createPurchaseHistory(any());
            verify(recordUseCase).getRecord(any());
            verify(recordUseCase).updateRecord(any());
        }
    }

    @Test
    void handleWebhook_ValidSignature() {
        // Given
        String payload = """
        {
            "type": "checkout.session.completed",
            "data": {
                "object": {
                    "metadata": {
                        "items": "[]",
                        "isGuest": "true"
                    }
                }
            }
        }""";
        String sigHeader = "test_signature";

        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("checkout.session.completed");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // When
            ResponseEntity<String> response = stripeUseCase.handleWebhook(payload, sigHeader);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Checkout session processed successfully", response.getBody());

            // Verify no processing occurred since items array is empty
            verify(purchaseHistoryUseCase, never()).createPurchaseHistory(any());
            verify(recordUseCase, never()).getRecord(any());
            verify(recordUseCase, never()).updateRecord(any());
        }
    }

    @Test
    void handleWebhook_EmptyItems()  {
        // Given
        String payload = """
        {
            "type": "checkout.session.completed",
            "data": {
                "object": {
                    "metadata": {
                        "items": "[]",
                        "isGuest": "true"
                    }
                }
            }
        }""";
        String sigHeader = "test_sig";

        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("checkout.session.completed");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // When
            ResponseEntity<String> response = stripeUseCase.handleWebhook(payload, sigHeader);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Checkout session processed successfully", response.getBody());

            // Verify no processing occurred
            verify(purchaseHistoryUseCase, never()).createPurchaseHistory(any());
            verify(recordUseCase, never()).getRecord(any());
            verify(recordUseCase, never()).updateRecord(any());
        }
    }

    @Test
    void handleWebhook_InvalidSignature()  {
        // Given
        String payload = "test_payload";
        String sigHeader = "invalid_signature";

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenThrow(new com.stripe.exception.SignatureVerificationException("Invalid signature", sigHeader));

            // When
            ResponseEntity<String> response = stripeUseCase.handleWebhook(payload, sigHeader);

            // Then
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody().contains("Webhook processing failed"));
        }
    }

    @Test
    void handleWebhook_NonCheckoutEvent() {
        // Given
        String payload = """
                {
                    "type": "payment_intent.succeeded",
                    "data": {
                        "object": {}
                    }
                }""";
        String sigHeader = "test_sig";

        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("payment_intent.succeeded");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // When
            ResponseEntity<String> response = stripeUseCase.handleWebhook(payload, sigHeader);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Webhook processed successfully", response.getBody());
        }
    }

    @Test
    void verifySession_InvalidSession() {
        // Given
        String sessionId = "invalid_session";

        // When
        boolean result = stripeUseCase.verifySession(sessionId);

        // Then
        assertFalse(result);
    }


    @Test
    void createCheckoutSession_MultipleItems()  {
        // Given
        CheckoutRequest.Item item1 = new CheckoutRequest.Item();
        item1.setTitle("Album 1");
        item1.setArtist("Artist 1");
        item1.setPrice(29.99);
        item1.setQuantity(1);
        item1.setCondition("New");

        CheckoutRequest.Item item2 = new CheckoutRequest.Item();
        item2.setTitle("Album 2");
        item2.setArtist("Artist 2");
        item2.setPrice(39.99);
        item2.setQuantity(3);
        item2.setCondition("Used");

        checkoutRequest.setItems(java.util.Arrays.asList(item1, item2));

        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);
        when(mockSession.getClientSecret()).thenReturn(TEST_CLIENT_SECRET);

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            // When
            CheckoutResponse response = stripeUseCase.createCheckoutSession(checkoutRequest, TEST_ORIGIN);

            // Then
            assertNotNull(response);
            assertEquals(TEST_SESSION_ID, response.getSessionId());
            assertEquals(TEST_CLIENT_SECRET, response.getClientSecret());
        }

    }
    @Test
    void verifySession_ValidSession()  {
        // Given
        String sessionId = "valid_session_id";

        // When
        boolean result = stripeUseCase.verifySession(sessionId);

        // Then
        assertFalse(result); // Should be false in test environment
    }
}
