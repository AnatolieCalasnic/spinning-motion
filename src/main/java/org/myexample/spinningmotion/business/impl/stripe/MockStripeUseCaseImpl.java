package org.myexample.spinningmotion.business.impl.stripe;

import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.business.interfc.StripeUseCase;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
@Slf4j
public class MockStripeUseCaseImpl implements StripeUseCase {

    private static final String MOCK_SESSION_ID = "mock_session_123";
    private static final String MOCK_CLIENT_SECRET = "mock_client_secret_456";

    @Override
    public CheckoutResponse createCheckoutSession(CheckoutRequest request, String origin) {
        log.info("MOCK: Creating checkout session");
        log.info("MOCK: Return URL will be: {}", origin + "/success?session_id={CHECKOUT_SESSION_ID}");

        // Log the items being processed
        request.getItems().forEach(item -> {
            log.info("MOCK: Creating line item for: {} - {} units at €{}",
                    item.getTitle(),
                    item.getQuantity(),
                    item.getPrice());

            // Calculate and log the total for this item
            double itemTotal = item.getPrice() * item.getQuantity();
            log.info("MOCK: Item total: €{}", itemTotal);
        });

        // Calculate and log the total order amount
        double orderTotal = request.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        log.info("MOCK: Order total: €{}", orderTotal);

        // Return both session ID and client secret
        return new CheckoutResponse(MOCK_SESSION_ID, MOCK_CLIENT_SECRET);
    }

    @Override
    public ResponseEntity<String> handleWebhook(String payload, String sigHeader) {
        log.info("MOCK: Processing webhook");
        log.info("MOCK: Signature: {}", sigHeader);
        log.info("MOCK: Payload received: {}", payload);

        try {
            // Validate webhook signature header
            if (sigHeader == null || sigHeader.isEmpty()) {
                log.error("MOCK: Missing Stripe signature header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Missing Stripe signature header");
            }

            // Simulate processing delay
            Thread.sleep(100);

            // Log the mock success event
            log.info("MOCK: Successfully processed webhook event");
            log.info("MOCK: Session ID: {}", MOCK_SESSION_ID);
            log.info("MOCK: Payment status: success");

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (InterruptedException e) {
            log.error("MOCK: Webhook processing interrupted", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Mock webhook processing failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("MOCK: Unexpected error while processing webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }
    @Override
    public boolean verifySession(String sessionId) {
        log.info("MOCK: Verifying session: {}", sessionId);

        // For testing, verify if it matches our mock session ID
        boolean isValid = MOCK_SESSION_ID.equals(sessionId);

        if (isValid) {
            log.info("MOCK: Session verified successfully");
        } else {
            log.warn("MOCK: Invalid session ID");
        }

        return isValid;
    }
}