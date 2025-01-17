package org.myexample.spinningmotion.business.impl.email_confirmation;

import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.business.interfc.EmailUseCase;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("test")
@Slf4j
public class MockEmailUseCaseImpl implements EmailUseCase {
    @Override
    public void sendOrderConfirmation(String to, List<CheckoutRequest.Item> items,
                                      double totalAmount, String orderNumber) {
        log.info("MOCK: Would send emailtest to {} for order {}", to, orderNumber);
        log.info("MOCK: Order contains {} items with total amount: €{}", items.size(), totalAmount);
        items.forEach(item ->
                log.info("MOCK: Item - ID: {}, Title: {}, Quantity: {}, Price: €{}",
                        item.getRecordId(),
                        item.getTitle(),
                        item.getQuantity(),
                        item.getPrice())
        );
    }
    @Override
    public void sendNewReleaseNotification(String to, List<RecordEntity> newRecords) {
        log.info("MOCK: Would send new release notification to {}", to);
        log.info("MOCK: New Release Details - Title: {}, Artist: {}, Genre: {}, Price: €{}",
               newRecords);
    }
}