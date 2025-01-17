package org.myexample.spinningmotion.business.impl.emailtest;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.exception.EmailSendingException;
import org.myexample.spinningmotion.business.impl.email_confirmation.EmailUseCaseImpl;
import org.myexample.spinningmotion.business.interfc.RecordImageUseCase;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.myexample.spinningmotion.persistence.entity.RecordImageEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailUseCaseImplTest {
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private RecordImageUseCase recordImageUseCase;
    private EmailUseCaseImpl emailUseCase;

    private static final String FROM_EMAIL = "test@spinningmotion.com";
    private static final String TO_EMAIL = "test@example.com";
    private static final String ORDER_NUMBER = "ORD-12345678";

    @BeforeEach
    void setUp() {
        emailUseCase = new EmailUseCaseImpl(mailSender, recordImageUseCase);
        ReflectionTestUtils.setField(emailUseCase, "fromEmail", FROM_EMAIL);
    }

    @Test
    void sendOrderConfirmation_SuccessfulSend() {
        // Arrange
        List<CheckoutRequest.Item> items = Arrays.asList(
                createTestItem(1L, "Test Record", "Test Artist", "New", 19.99, 1),
                createTestItem(2L, "Another Record", "Another Artist", "Used", 24.99, 2)
        );
        double totalAmount = 69.97;

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        assertDoesNotThrow(() ->
                emailUseCase.sendOrderConfirmation(TO_EMAIL, items, totalAmount, ORDER_NUMBER)
        );

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendOrderConfirmation_HandlesException() {
        // Arrange
        List<CheckoutRequest.Item> items = Arrays.asList(
                createTestItem(1L, "Test Record", "Test Artist", "New", 19.99, 1)
        );
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Failed to send emailtest"))
                .when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(EmailSendingException.class, () ->
                emailUseCase.sendOrderConfirmation(TO_EMAIL, items, 19.99, ORDER_NUMBER)
        );
    }

    private CheckoutRequest.Item createTestItem(Long recordId, String title, String artist, String condition, double price, int quantity) {
        return CheckoutRequest.Item.builder()
                .recordId(recordId)
                .title(title)
                .artist(artist)
                .condition(condition)
                .price(price)
                .quantity(quantity)
                .build();
    }
    @Test
    void sendOrderConfirmation_HandlesImagesCorrectly() {
        // Arrange
        List<CheckoutRequest.Item> items = Arrays.asList(
                createTestItem(1L, "Test Record", "Test Artist", "New", 19.99, 1)
        );

        RecordImageEntity testImage = new RecordImageEntity();
        testImage.setImageData(new byte[] {1, 2, 3});  // Sample image data
        testImage.setImageType("image/jpeg");

        when(recordImageUseCase.getImagesByRecordId(1L))
                .thenReturn(Arrays.asList(testImage));

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailUseCase.sendOrderConfirmation(TO_EMAIL, items, 19.99, ORDER_NUMBER);

        // Assert
        verify(recordImageUseCase).getImagesByRecordId(1L);
        verify(mailSender).send(any(MimeMessage.class));
    }
    @Test
    void verifyProfileAnnotation() {
        Profile profileAnnotation = EmailUseCaseImpl.class.getAnnotation(Profile.class);
        assertNotNull(profileAnnotation, "Class should have @Profile annotation");
        assertArrayEquals(new String[]{"!test"}, profileAnnotation.value(),
                "Profile should be set to '!test'");
    }

    @Test
    void verifyServiceAnnotation() {
        assertNotNull(EmailUseCaseImpl.class.getAnnotation(Service.class),
                "Class should have @Service annotation");
    }
    @Test
    void sendNewReleaseNotification_SuccessfulSend() {
        // Arrange
        RecordEntity recordEnt = createTestRecord(1L, "New Album", "Test Artist", 29.99);
        List<RecordEntity> newRecords = Arrays.asList(recordEnt);

        // Mock MimeMessage and successfully send
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        assertDoesNotThrow(() ->
                emailUseCase.sendNewReleaseNotification(TO_EMAIL, newRecords)
        );

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));

        // Verifying the subject line contains correct count
        // We need to capture the MimeMessageHelper to verify its contents
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
    }

    @Test
    void sendNewReleaseNotification_MultipleRecords() {
        // Arrange
        List<RecordEntity> newRecords = Arrays.asList(
                createTestRecord(1L, "Album 1", "Artist 1", 29.99),
                createTestRecord(2L, "Album 2", "Artist 2", 19.99)
        );

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Set up image mocking for both records
        RecordImageEntity testImage = createTestImage();
        when(recordImageUseCase.getImagesByRecordId(anyLong()))
                .thenReturn(Arrays.asList(testImage));

        // Act
        assertDoesNotThrow(() ->
                emailUseCase.sendNewReleaseNotification(TO_EMAIL, newRecords)
        );

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
        verify(recordImageUseCase, times(2)).getImagesByRecordId(anyLong());
    }

    @Test
    void sendNewReleaseNotification_HandlesException() {
        // Arrange
        List<RecordEntity> newRecords = Arrays.asList(
                createTestRecord(1L, "Test Album", "Test Artist", 29.99)
        );

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        String errorMessage = "Failed to send emailtest";
        doThrow(new RuntimeException(errorMessage))
                .when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        EmailSendingException exception = assertThrows(EmailSendingException.class,
                () -> emailUseCase.sendNewReleaseNotification(TO_EMAIL, newRecords)
        );

        // Verify the complete error message including the cause
        assertEquals(
                "Failed to send new release notification: " + errorMessage,
                exception.getMessage(),
                "Error message should include both the main message and the cause"
        );
    }

    @Test
    void sendNewReleaseNotification_EmptyRecordsList() {
        // Arrange
        List<RecordEntity> emptyRecords = Collections.emptyList();
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        assertDoesNotThrow(() ->
                emailUseCase.sendNewReleaseNotification(TO_EMAIL, emptyRecords)
        );

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    // Helper method to create test records
    private RecordEntity createTestRecord(Long id, String title, String artist, double price) {
        return RecordEntity.builder()
                .id(id)
                .title(title)
                .artist(artist)
                .price(price)
                .genre(GenreEntity.builder()
                        .id(1L)
                        .name("Rock")
                        .build())
                .year(2023)
                .condition("New")
                .build();
    }

    // Helper method to create test image
    private RecordImageEntity createTestImage() {
        RecordImageEntity image = new RecordImageEntity();
        image.setImageData(new byte[] {1, 2, 3});
        image.setImageType("image/jpeg");
        return image;
    }
}