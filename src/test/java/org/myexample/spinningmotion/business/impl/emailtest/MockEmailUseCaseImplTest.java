package org.myexample.spinningmotion.business.impl.emailtest;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.impl.email_confirmation.MockEmailUseCaseImpl;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MockEmailUseCaseImplTest {

    private MockEmailUseCaseImpl mockEmailUseCase;
    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        mockEmailUseCase = new MockEmailUseCaseImpl();
        logger = (Logger) LoggerFactory.getLogger(MockEmailUseCaseImpl.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @Test
    void sendOrderConfirmation_ShouldLogOrderDetails() {
        // Given
        String email = "test@example.com";
        String orderNumber = "ORD-12345";
        double totalAmount = 59.98;

        List<CheckoutRequest.Item> items = Arrays.asList(
                createTestItem(1L, "Album 1", 29.99, 1),
                createTestItem(2L, "Album 2", 29.99, 1)
        );

        // When
        mockEmailUseCase.sendOrderConfirmation(email, items, totalAmount, orderNumber);

        // Then
        List<String> logMessages = logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        // Verify each expected log message individually
        assertTrue(logMessages.contains(
                        String.format("MOCK: Would send emailtest to %s for order %s", email, orderNumber)),
                "Should log emailtest and order number");

        assertTrue(logMessages.contains(
                        String.format("MOCK: Order contains %d items with total amount: €%.2f",
                                items.size(), totalAmount)),
                "Should log item count and total amount");

        // Verify item details
        for (CheckoutRequest.Item item : items) {
            String expectedItemLog = String.format(
                    "MOCK: Item - ID: %d, Title: %s, Quantity: %d, Price: €%.2f",
                    item.getRecordId(), item.getTitle(), item.getQuantity(), item.getPrice());
            assertTrue(logMessages.contains(expectedItemLog),
                    "Should log details for item: " + item.getTitle());
        }
    }

    @Test
    void sendNewReleaseNotification_ShouldLogNewReleaseDetails() {
        // Given
        String email = "subscriber@example.com";
        RecordEntity albumEntity = createTestRecord("New Album", "Artist", "Rock", 29.99);
        List<RecordEntity> newRecords = List.of(albumEntity);

        // When
        mockEmailUseCase.sendNewReleaseNotification(email, newRecords);

        // Then
        List<String> logMessages = logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        assertTrue(logMessages.contains(
                        String.format("MOCK: Would send new release notification to %s", email)),
                "Should log notification recipient");

        // The second log message contains the record details
        String recordDetailsLog = logMessages.get(1);
        assertTrue(recordDetailsLog.contains("New Album"), "Should contain album title");
        assertTrue(recordDetailsLog.contains("Artist"), "Should contain artist name");
        assertTrue(recordDetailsLog.contains("Rock"), "Should contain genre");
        assertTrue(recordDetailsLog.contains("29.99"), "Should contain price");
    }

    @Test
    void sendOrderConfirmation_WithEmptyItemList_ShouldLogAppropriately() {
        // Given
        String email = "test@example.com";
        String orderNumber = "ORD-12345";
        List<CheckoutRequest.Item> items = Collections.emptyList();

        // When
        mockEmailUseCase.sendOrderConfirmation(email, items, 0.0, orderNumber);

        // Then
        List<String> logMessages = logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        // First message verification
        String expectedEmailMessage = String.format(
                "MOCK: Would send emailtest to %s for order %s",
                email,
                orderNumber
        );
        assertTrue(logMessages.contains(expectedEmailMessage),
                "Should log empty order emailtest notification");

        // Second message verification - matching the exact format from the implementation
        String expectedOrderMessage = "MOCK: Order contains 0 items with total amount: €0.0";
        assertTrue(logMessages.contains(expectedOrderMessage),
                "Should log empty order details");
    }


    // Helper methods to create test data
    private CheckoutRequest.Item createTestItem(Long recordId, String title, double price, int quantity) {
        CheckoutRequest.Item item = new CheckoutRequest.Item();
        item.setRecordId(recordId);
        item.setTitle(title);
        item.setPrice(price);
        item.setQuantity(quantity);
        return item;
    }

    private RecordEntity createTestRecord(String title, String artist, String genre, double price) {
        return RecordEntity.builder()
                .title(title)
                .artist(artist)
                .genre(GenreEntity.builder()
                        .name(genre)
                        .build())
                .price(price)
                .build();
    }
}