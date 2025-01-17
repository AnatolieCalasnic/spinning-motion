package org.myexample.spinningmotion.business.impl.stripetest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.impl.stripe.StripeUseCaseImpl;
import org.myexample.spinningmotion.business.interfc.*;
import org.myexample.spinningmotion.domain.purchase_history.CreatePurchaseHistoryRequest;
import org.myexample.spinningmotion.domain.record.GetRecordResponse;
import org.myexample.spinningmotion.domain.record.UpdateRecordRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutResponse;
import org.myexample.spinningmotion.domain.user.GetUserRequest;
import org.myexample.spinningmotion.domain.user.GetUserResponse;
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
    @Mock
    private EmailUseCase emailUseCase; // Add this mock
    @Mock
    private UserUseCase userUseCase; // Add this mock
    @Mock
    private CouponUseCase couponUseCase; // Add this mock
    @InjectMocks
    private StripeUseCaseImpl stripeUseCase;

    private CheckoutRequest checkoutRequest;
    private static final String TEST_ORIGIN = "http://localhost:3000";
    private static final String TEST_SESSION_ID = "test_session_123";
    private static final String TEST_CLIENT_SECRET = "test_client_secret_456";
    private static final String TEST_SECRET_KEY = "sk_test_123";
    private static final String TEST_WEBHOOK_SECRET = "whsec_123";
    private ObjectMapper objectMapper = new ObjectMapper();

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
        testItem.setRecordId(1L);

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
    void handleWebhook_Success() {
        String testItems = "[{\\\"recordId\\\":1,\\\"title\\\":\\\"Test Record\\\",\\\"artist\\\":\\\"Test Artist\\\",\\\"quantity\\\":1,\\\"price\\\":29.99,\\\"condition\\\":\\\"New\\\"}]";
        String payload = """
            {
                "type": "checkout.session.completed",
                "data": {
                    "object": {
                        "metadata": {
                            "items": "%s",
                            "isGuest": "true",
                            "guestDetails": "{\\"email\\":\\"test@example.com\\"}"
                        }
                    }
                }
            }""".formatted(testItems);

        String sigHeader = "test_sig";

        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("checkout.session.completed");

        // Mock record use case responses
        when(recordUseCase.getRecord(any()))
                .thenReturn(GetRecordResponse.builder()
                        .id(1L)
                        .title("Test Record")
                        .artist("Test Artist")
                        .quantity(5)
                        .price(29.99)
                        .condition("New")
                        .build());

        // Mock emailtest service to avoid NPE
        doNothing().when(emailUseCase).sendOrderConfirmation(
                anyString(), anyList(), anyDouble(), anyString());

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            ResponseEntity<String> response = stripeUseCase.handleWebhook(payload, sigHeader);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(recordUseCase).getRecord(any());
            verify(recordUseCase).updateRecord(any());
            verify(emailUseCase).sendOrderConfirmation(
                    eq("test@example.com"),
                    anyList(),
                    anyDouble(),
                    anyString()
            );
        }
    }

    @Test
    void handleWebhook_WithCouponSuccess() {
        // Given
        String couponCode = "TEST123";
        int discountPercentage = 20;
        String payload = createWebhookPayloadWithCoupon(couponCode, discountPercentage);
        String sigHeader = "test_sig";

        // Mock the Webhook event
        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("checkout.session.completed");

        // Mock coupon validation
        when(couponUseCase.validateCoupon(couponCode)).thenReturn(true);

        // Mock record lookup
        when(recordUseCase.getRecord(any())).thenReturn(
                GetRecordResponse.builder()
                        .id(1L)
                        .title("Test Record")
                        .artist("Test Artist")
                        .quantity(5)
                        .price(100.0)
                        .condition("New")
                        .build()
        );

        // Mock email service to avoid errors
        doNothing().when(emailUseCase).sendOrderConfirmation(
                anyString(), anyList(), anyDouble(), anyString()
        );

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            // Verify Webhook event construction
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // When
            ResponseEntity<String> response = stripeUseCase.handleWebhook(payload, sigHeader);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());

            // Verify coupon usage
            verify(couponUseCase).validateCoupon(couponCode);
            verify(couponUseCase).markCouponAsUsed(couponCode);

            // Verify record processing
            verify(recordUseCase).getRecord(any());
            verify(recordUseCase).updateRecord(any());

            // Verify email confirmation
            verify(emailUseCase).sendOrderConfirmation(
                    eq("test@example.com"),
                    anyList(),
                    anyDouble(),
                    anyString()
            );
        }
    }
    @Test
    void handleWebhook_RegisteredUserFlow() {
        // Given a webhook payload for a registered user purchase
        Long userId = 123L;
        String userEmail = "user@test.com";
        long recordId = 1L;
        int recordQuantity = 5;
        double recordPrice = 100.0;

        // Mock the Webhook event
        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("checkout.session.completed");
        String sigHeader = "test_sig";
        String payload = createWebhookPayloadForRegisteredUser(userId);

        // Mock user lookup
        GetUserResponse mockUser = GetUserResponse.builder()
                .id(userId)
                .email(userEmail)
                .build();
        when(userUseCase.getUser(argThat(req -> req.getId().equals(userId))))
                .thenReturn(mockUser);

        // Mock record lookup
        GetRecordResponse mockRecord = GetRecordResponse.builder()
                .id(recordId)
                .title("Test Record")
                .artist("Test Artist")
                .quantity(recordQuantity)
                .price(recordPrice)
                .condition("New")
                .build();
        when(recordUseCase.getRecord(argThat(req -> req.getId().equals(recordId))))
                .thenReturn(mockRecord);

        // Mock Stripe webhook verification
        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // When processing the webhook
            ResponseEntity<String> response = stripeUseCase.handleWebhook(payload, sigHeader);

            // Then expect successful processing
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Checkout session processed successfully", response.getBody());

            // And verify user validation flow
            verify(userUseCase, times(2)).getUser(any(GetUserRequest.class));

            // And verify record updates
            ArgumentCaptor<UpdateRecordRequest> recordUpdateCaptor = ArgumentCaptor.forClass(UpdateRecordRequest.class);
            verify(recordUseCase).updateRecord(recordUpdateCaptor.capture());
            UpdateRecordRequest recordUpdate = recordUpdateCaptor.getValue();
            assertEquals(recordId, recordUpdate.getId());
            assertEquals(recordQuantity - 1, recordUpdate.getQuantity()); // -1 for the purchased quantity

            // And verify purchase history creation
            ArgumentCaptor<CreatePurchaseHistoryRequest> purchaseCaptor = ArgumentCaptor.forClass(CreatePurchaseHistoryRequest.class);
            verify(purchaseHistoryUseCase).createPurchaseHistory(purchaseCaptor.capture());
            CreatePurchaseHistoryRequest purchase = purchaseCaptor.getValue();
            assertEquals(userId, purchase.getUserId());
            assertEquals(recordId, purchase.getRecordId());
            assertFalse(purchase.isGuest());

            // And verify email notification
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
            verify(emailUseCase).sendOrderConfirmation(
                    emailCaptor.capture(),
                    anyList(),
                    amountCaptor.capture(),
                    anyString()
            );
            assertEquals(userEmail, emailCaptor.getValue());
            assertEquals(recordPrice, amountCaptor.getValue(), 0.01);
        }
    }

    @Test
    void handleWebhook_ValidSignature() {
        String payload = """
    {
        "type": "checkout.session.completed",
        "data": {
            "object": {
                "metadata": {
                    "items": "[{\\"recordId\\":1,\\"title\\":\\"Test Record\\",\\"artist\\":\\"Test Artist\\",\\"quantity\\":1,\\"price\\":29.99,\\"condition\\":\\"New\\"}]",
                    "isGuest": "true",
                    "guestDetails": "{\\"email\\":\\"test@example.com\\"}"
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

            when(recordUseCase.getRecord(any()))
                    .thenReturn(GetRecordResponse.builder()
                            .id(1L)
                            .title("Test Record")
                            .artist("Test Artist")
                            .quantity(5)
                            .price(29.99)
                            .condition("New")
                            .build());

            ResponseEntity<String> response = stripeUseCase.handleWebhook(payload, sigHeader);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(recordUseCase).getRecord(any());
            verify(recordUseCase).updateRecord(any());
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
    void handleWebhook_PurchaseHistoryCreationFails_ThrowsPurchaseProcessingException() {
        // Given
        String testItems = "[{\\\"recordId\\\":1,\\\"title\\\":\\\"Test Record\\\",\\\"artist\\\":\\\"Test Artist\\\",\\\"quantity\\\":1,\\\"price\\\":29.99,\\\"condition\\\":\\\"New\\\"}]";
        String payload = """
        {
            "type": "checkout.session.completed",
            "data": {
                "object": {
                    "metadata": {
                        "items": "%s",
                        "isGuest": "true",
                        "guestDetails": "{\\"email\\":\\"test@example.com\\"}"
                    }
                }
            }
        }""".formatted(testItems);
        String sigHeader = "test_sig";

        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("checkout.session.completed");

        when(recordUseCase.getRecord(any())).thenReturn(
                GetRecordResponse.builder()
                        .id(1L)
                        .title("Test Record")
                        .artist("Test Artist")
                        .quantity(5)
                        .price(29.99)
                        .condition("New")
                        .build());

        // Mock purchase history creation to fail
        doThrow(new RuntimeException("Purchase history creation failed"))
                .when(purchaseHistoryUseCase).createPurchaseHistory(any());

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // When
            ResponseEntity<String> response = stripeUseCase.handleWebhook(payload, sigHeader);

            // Then
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody().contains("Failed to process checkout session"));

            // Verify record retrieval was attempted
            verify(recordUseCase).getRecord(any());
            // Verify purchase history creation was attempted
            verify(purchaseHistoryUseCase).createPurchaseHistory(any());
        }
    }

    @Test
    void handleWebhook_InvalidItemsJson_ThrowsPurchaseProcessingException() {
        // Given
        String payload = """
    {
        "type": "checkout.session.completed",
        "data": {
            "object": {
                "metadata": {
                    "items": "invalid-json",
                    "isGuest": "true",
                    "guestDetails": "{\\"email\\":\\"test@example.com\\"}"
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
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().toLowerCase().contains("failed to process checkout session"),
                    "Expected response body to contain error message, but got: " + response.getBody());

            // Verify that record service was never called
            verify(recordUseCase, never()).getRecord(any());
        }
    }

    @Test
    void handleWebhook_MissingMetadata_ThrowsPurchaseProcessingException() {
        // Given
        String payload = """
    {
        "type": "checkout.session.completed",
        "data": {
            "object": {
                "metadata": {}
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
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().toLowerCase().contains("failed to process checkout session"),
                    "Expected response body to contain error message, but got: " + response.getBody());

            // Verify record service was never called
            verify(recordUseCase, never()).getRecord(any());
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

    private String createWebhookPayloadWithCoupon(String couponCode, int discountPercentage) {
        CheckoutRequest.Item item = new CheckoutRequest.Item();
        item.setRecordId(1L);
        item.setTitle("Test Record");
        item.setArtist("Test Artist");
        item.setQuantity(1);
        item.setPrice(100.0);
        item.setCondition("New");

        try {
            String itemsJson = objectMapper.writeValueAsString(Collections.singletonList(item))
                    .replace("\"", "\\\""); // Escape quotes for JSON string

            return """
                {
                    "type": "checkout.session.completed",
                    "data": {
                        "object": {
                            "metadata": {
                                "items": "%s",
                                "isGuest": "true",
                                "couponCode": "%s",
                                "couponDiscount": "%d",
                                "guestDetails": "{\\"email\\":\\"test@example.com\\"}"
                            }
                        }
                    }
                }""".formatted(itemsJson, couponCode, discountPercentage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create webhook payload", e);
        }
    }

    private String createWebhookPayloadForRegisteredUser(Long userId) {
        CheckoutRequest.Item item = new CheckoutRequest.Item();
        item.setRecordId(1L);
        item.setTitle("Test Record");
        item.setArtist("Test Artist");
        item.setQuantity(1);
        item.setPrice(100.0);
        item.setCondition("New");

        try {
            String itemsJson = objectMapper.writeValueAsString(Collections.singletonList(item))
                    .replace("\"", "\\\""); // Escape quotes for JSON string

            return """
                {
                    "type": "checkout.session.completed",
                    "data": {
                        "object": {
                            "metadata": {
                                "items": "%s",
                                "isGuest": "false",
                                "userId": "%d"
                            }
                        }
                    }
                }""".formatted(itemsJson, userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create webhook payload", e);
        }
    }
}