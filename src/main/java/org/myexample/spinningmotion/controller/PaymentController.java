package org.myexample.spinningmotion.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.business.interfc.StripeUseCase;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutResponse;
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
        return ResponseEntity.ok(stripeUseCase.createCheckoutSession(request, origin));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        return stripeUseCase.handleWebhook(payload, sigHeader);
    }


    @PostMapping("/verify-session/{sessionId}")
    public ResponseEntity<Map<String, Boolean>> verifySession(@PathVariable String sessionId) {
        boolean isValid = stripeUseCase.verifySession(sessionId);
        Map<String, Boolean> response = Map.of("success", isValid);
        return ResponseEntity.ok(response);
    }
}
