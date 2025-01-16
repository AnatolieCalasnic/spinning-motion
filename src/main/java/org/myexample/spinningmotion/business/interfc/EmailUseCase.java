package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;

import java.util.List;

public interface EmailUseCase {
    void sendOrderConfirmation(String to, List<CheckoutRequest.Item> items,
                               double totalAmount, String orderNumber);
    void sendNewReleaseNotification(String to, List<RecordEntity> newRecords);

}