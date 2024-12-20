package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutResponse;
import org.springframework.http.ResponseEntity;

public interface StripeUseCase {
    CheckoutResponse createCheckoutSession(CheckoutRequest request, String origin);
    ResponseEntity<String> handleWebhook(String payload, String sigHeader);
    boolean verifySession(String sessionId);
}
