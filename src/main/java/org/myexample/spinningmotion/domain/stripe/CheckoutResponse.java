package org.myexample.spinningmotion.domain.stripe;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckoutResponse {
    private String sessionId;
    private String clientSecret;

    public CheckoutResponse(String sessionId) {
        this.sessionId = sessionId;
        this.clientSecret = null;
    }
    public CheckoutResponse(String sessionId, String clientSecret) {
        this.sessionId = sessionId;
        this.clientSecret = clientSecret;
    }
}