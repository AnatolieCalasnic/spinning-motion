package org.myexample.spinningmotion.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.business.exception.InvalidInputException;
import org.myexample.spinningmotion.business.interfc.StripeUseCase;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
@AllArgsConstructor
public class PaymentController {
    private final StripeUseCase stripeUseCase;
    @PostMapping("/create-checkout-session")
    public ResponseEntity<CheckoutResponse> createCheckoutSession(
            @RequestBody CheckoutRequest request,
            @RequestHeader("Origin") String origin) {
        if (request.getItems() != null && request.getItems().size() > 10) {
            throw new InvalidInputException("Maximum 10 items allowed per checkout");
        }
        return ResponseEntity.ok(stripeUseCase.createCheckoutSession(request, origin));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            log.info("Received Stripe Webhook Payload: {}", payload);
            log.info("Stripe Signature: {}", sigHeader);

            return stripeUseCase.handleWebhook(payload, sigHeader);
        } catch (Exception e) {
            log.error("Webhook Processing Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook processing failed: " + e.getMessage());
        }
    }


    @PostMapping("/verify-session/{sessionId}")
    public ResponseEntity<Map<String, Boolean>> verifySession(@PathVariable String sessionId) {
        boolean isValid = stripeUseCase.verifySession(sessionId);
        Map<String, Boolean> response = Map.of("success", isValid);
        return ResponseEntity.ok(response);
    }
}
