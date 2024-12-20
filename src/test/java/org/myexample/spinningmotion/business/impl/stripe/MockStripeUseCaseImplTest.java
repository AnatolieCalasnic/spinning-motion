package org.myexample.spinningmotion.business.impl.stripe;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.domain.guest_user.GuestDetails;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MockStripeUseCaseImplTest {

    @InjectMocks
    private MockStripeUseCaseImpl mockStripeUseCase;

    private CheckoutRequest checkoutRequest;
    private static final String TEST_ORIGIN = "http://localhost:3000";
    private static final String MOCK_SESSION_ID = "mock_session_123";
    private static final String MOCK_CLIENT_SECRET = "mock_client_secret_456";


    @BeforeEach
    void setUp() {
        CheckoutRequest.Item testItem = new CheckoutRequest.Item();
        testItem.setTitle("Test Product");
        testItem.setArtist("Test Artist");
        testItem.setPrice(19.99);
        testItem.setQuantity(2);
        testItem.setCondition("New");

        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setItems(Collections.singletonList(testItem));
    }

    @Test
    void createCheckoutSession_Success() {
        CheckoutResponse response = mockStripeUseCase.createCheckoutSession(checkoutRequest, TEST_ORIGIN);

        // Then
        assertNotNull(response);
        assertEquals(MOCK_SESSION_ID, response.getSessionId());
        assertEquals(MOCK_CLIENT_SECRET, response.getClientSecret());
    }
    @Test
    void createCheckoutSession_WithGuestDetails() {
        // Given
        GuestDetails guestDetails = new GuestDetails();
        guestDetails.setEmail("test@example.com");
        guestDetails.setFname("Test");
        guestDetails.setLname("User");
        checkoutRequest.setGuestDetails(guestDetails);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("isGuest", "true");
        checkoutRequest.setMetadata(metadata);

        // When
        CheckoutResponse response = mockStripeUseCase.createCheckoutSession(checkoutRequest, TEST_ORIGIN);

        // Then
        assertNotNull(response);
        assertEquals(MOCK_SESSION_ID, response.getSessionId());
        assertEquals(MOCK_CLIENT_SECRET, response.getClientSecret());
    }
    @Test
    void createCheckoutSession_MultipleItems() {
        // Given
        CheckoutRequest.Item item1 = new CheckoutRequest.Item();
        item1.setTitle("Product 1");
        item1.setArtist("Artist 1");
        item1.setPrice(29.99);
        item1.setQuantity(1);
        item1.setCondition("New");

        CheckoutRequest.Item item2 = new CheckoutRequest.Item();
        item2.setTitle("Product 2");
        item2.setArtist("Artist 2");
        item2.setPrice(39.99);
        item2.setQuantity(3);
        item2.setCondition("Used");

        checkoutRequest.setItems(Arrays.asList(item1, item2));

        // When
        CheckoutResponse response = mockStripeUseCase.createCheckoutSession(checkoutRequest, TEST_ORIGIN);

        // Then
        assertNotNull(response);
        assertEquals(MOCK_SESSION_ID, response.getSessionId());
        assertEquals(MOCK_CLIENT_SECRET, response.getClientSecret());

    }

    @Test
    void createCheckoutSession_EmptyItems() {
        // Given
        checkoutRequest.setItems(Collections.emptyList());

        // When
        CheckoutResponse response = mockStripeUseCase.createCheckoutSession(checkoutRequest, TEST_ORIGIN);

        // Then
        assertNotNull(response);
        assertEquals(MOCK_SESSION_ID, response.getSessionId());
        assertEquals(MOCK_CLIENT_SECRET, response.getClientSecret());
    }

    @Test
    void handleWebhook_ValidSignature() {
        // Given
        String testPayload = "{\"type\":\"checkout.session.completed\"}";
        String testSignature = "valid_signature";

        // When
        ResponseEntity<String> response = mockStripeUseCase.handleWebhook(testPayload, testSignature);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Webhook processed successfully", response.getBody());
    }
    @Test
    void handleWebhook_MissingSignature() {
        // Given
        String testPayload = "{\"type\":\"checkout.session.completed\"}";

        // When
        ResponseEntity<String> response = mockStripeUseCase.handleWebhook(testPayload, null);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Missing Stripe signature header", response.getBody());
    }

    @Test
    void handleWebhook_NullPayload() {
        // Given
        String testSignature = "test_signature";

        // When/Then
        assertDoesNotThrow(() -> mockStripeUseCase.handleWebhook(null, testSignature));
    }

    @Test
    void handleWebhook_NullSignature() {
        // Given
        String testPayload = "{\"type\":\"checkout.session.completed\"}";

        // When/Then
        assertDoesNotThrow(() -> mockStripeUseCase.handleWebhook(testPayload, null));
    }

    @Test
    void createCheckoutSession_NullRequest() {
        // When/Then
        assertThrows(NullPointerException.class,
                () -> mockStripeUseCase.createCheckoutSession(null, TEST_ORIGIN));    }

    @Test
    void createCheckoutSession_NullOrigin() {
        // When/Then
        assertDoesNotThrow(() -> mockStripeUseCase.createCheckoutSession(checkoutRequest, null));
    }
    @Test
    void verifySession_ValidSession() {
        // When
        boolean result = mockStripeUseCase.verifySession(MOCK_SESSION_ID);

        // Then
        assertTrue(result);
    }

    @Test
    void verifySession_InvalidSession() {
        // When
        boolean result = mockStripeUseCase.verifySession("invalid_session_id");

        // Then
        assertFalse(result);
    }
}