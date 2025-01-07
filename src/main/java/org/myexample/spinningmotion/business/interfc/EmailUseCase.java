package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;

import java.util.List;

public interface EmailUseCase {
    void sendOrderConfirmation(String to, List<CheckoutRequest.Item> items,
                               double totalAmount, String orderNumber);
}